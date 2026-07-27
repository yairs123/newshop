package com.coinmarket.common.storage;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.storage.FileStorageService.StoreResult;
import com.coinmarket.common.util.BarcodeUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传和管理接口")
public class FileController {

    private final FileStorageService storageService;

    @Value("${app.file.max-size:10485760}")
    private long maxFileSize;

    private static final byte[][] IMAGE_MAGIC_BYTES = {
        {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},           // JPEG
        {(byte) 0x89, 0x50, 0x4E, 0x47},                     // PNG
        {0x47, 0x49, 0x46},                                    // GIF
        {0x52, 0x49, 0x46, 0x46}                               // WEBP (RIFF header)
    };

    /**
     * Upload a single image for a product identified by barcode.
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传文件", description = "上传商品图片")
    public ApiResponse<Map<String, String>> upload(
            @RequestParam String barcode,
            @RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("File is empty");
        }
        if (file.getSize() > maxFileSize) {
            return ApiResponse.error("File size exceeds maximum allowed (" + maxFileSize + " bytes)");
        }
        if (!BarcodeUtil.isValid(barcode)) {
            return ApiResponse.error("Invalid barcode: " + barcode);
        }
        String validationError = validateImageContent(file);
        if (validationError != null) {
            return ApiResponse.error(validationError);
        }

        try {
            String filename = sanitizeFilename(file.getOriginalFilename(), barcode);
            StoreResult result = storageService.storeProductImage(barcode, filename, file.getBytes());

            return ApiResponse.success(Map.of(
                    "path", result.path(),
                    "url", result.url(),
                    "barcode", result.barcode()
            ));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (IOException e) {
            log.error("File upload failed", e);
            return ApiResponse.error("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Batch import: upload multiple files. Filenames must contain barcode prefix.
     * Accepted: "762202220222.01.jpg" or "762202220222_01.png"
     */
    @PostMapping("/import")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "批量导入", description = "批量导入商品图片")
    public ApiResponse<Map<String, Object>> batchImport(@RequestParam List<MultipartFile> files) {
        List<Map<String, String>> succeeded = new ArrayList<>();
        List<Map<String, String>> failed = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                failed.add(Map.of("file", "unknown", "reason", "Empty filename"));
                continue;
            }
            if (file.getSize() > maxFileSize) {
                failed.add(Map.of("file", originalName, "reason", "File size exceeds maximum allowed"));
                continue;
            }
            String validationError = validateImageContent(file);
            if (validationError != null) {
                failed.add(Map.of("file", originalName, "reason", validationError));
                continue;
            }
            String barcode = BarcodeUtil.fromFilename(originalName);
            if (barcode == null) {
                failed.add(Map.of("file", originalName, "reason", "No valid barcode in filename"));
                continue;
            }

            try {
                String filename = sanitizeFilename(originalName, barcode);
                StoreResult result = storageService.storeProductImage(barcode, filename, file.getBytes());

                succeeded.add(Map.of(
                        "file", originalName,
                        "path", result.path(),
                        "barcode", result.barcode()
                ));
            } catch (IllegalArgumentException e) {
                failed.add(Map.of("file", originalName, "reason", e.getMessage()));
            } catch (IOException e) {
                failed.add(Map.of("file", originalName, "reason", "Store failed: " + e.getMessage()));
            }
        }

        return ApiResponse.success(Map.of(
                "succeeded", succeeded,
                "failed", failed,
                "total", files.size(),
                "successCount", succeeded.size(),
                "failCount", failed.size()
        ));
    }

    /**
     * Upload a file without requiring a barcode (e.g., advertisement images).
     */
    @PostMapping("/upload/general")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "通用上传", description = "上传通用文件（广告图片等），返回文件URL")
    public ApiResponse<Map<String, String>> uploadGeneral(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("File is empty");
        }
        if (file.getSize() > maxFileSize) {
            return ApiResponse.error("File size exceeds maximum allowed (" + maxFileSize + " bytes)");
        }
        String validationError = validateImageContent(file);
        if (validationError != null) {
            return ApiResponse.error(validationError);
        }
        try {
            String originalName = file.getOriginalFilename();
            String sanitized = (originalName != null) ? originalName.replaceAll("[^a-zA-Z0-9._-]", "_") : "file";
            String filename = System.currentTimeMillis() + "_" + sanitized;
            String path = storageService.store("general", filename, file.getBytes());
            return ApiResponse.success(Map.of(
                    "url", storageService.getBaseUrl() + "/" + path,
                    "path", path
            ));
        } catch (IOException e) {
            log.error("File upload failed", e);
            return ApiResponse.error("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Serve stored files.
     */
    @GetMapping("/{barcode}/{filename:.+}")
    @Operation(summary = "获取文件", description = "获取存储的商品图片文件")
    public ResponseEntity<?> serveFile(
            @PathVariable String barcode,
            @PathVariable String filename) {
        String relativePath = barcode + "/" + filename;
        byte[] data = storageService.load(relativePath);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        String contentType = detectContentType(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .body(data);
    }

    /**
     * List files for a given barcode.
     */
    @GetMapping("/{barcode}")
    @Operation(summary = "获取文件列表", description = "获取指定商品的所有文件列表")
    public ApiResponse<List<String>> listFiles(@PathVariable String barcode) {
        List<String> files = storageService.listByBarcode(barcode);
        return ApiResponse.success(files);
    }

    private String sanitizeFilename(String original, String barcode) {
        if (original == null) return barcode + ".jpg";
        // Strip path, keep only filename
        String name = original.substring(original.lastIndexOf('/') + 1);
        name = name.substring(name.lastIndexOf('\\') + 1);
        // Remove special chars
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return name;
    }

    /**
     * Validates that the uploaded file is a supported image type using magic bytes.
     * Also checks file extension as a secondary guard.
     * @return error message if validation fails, null if valid
     */
    private String validateImageContent(MultipartFile file) {
        if (file.isEmpty()) {
            return "File is empty";
        }

        // Check file extension
        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            String lower = originalName.toLowerCase();
            boolean validExt = lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                    || lower.endsWith(".png") || lower.endsWith(".gif")
                    || lower.endsWith(".webp");
            if (!validExt) {
                return "Unsupported file type: " + originalName.substring(originalName.lastIndexOf('.') + 1);
            }
        }

        // Check magic bytes
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int bytesRead = is.read(header, 0, 4);
            if (bytesRead < 3) {
                return "Unable to read file content";
            }

            // Check JPEG (FF D8 FF)
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                return null; // Valid JPEG
            }
            // Check PNG (89 50 4E 47)
            if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return null; // Valid PNG
            }
            // Check GIF (47 49 46)
            if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46) {
                return null; // Valid GIF
            }
            // Check WebP (52 49 46 46 - RIFF header)
            if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46) {
                return null; // Valid WebP (RIFF)
            }

            return "File content does not match supported image formats (JPEG, PNG, GIF, WebP)";
        } catch (IOException e) {
            log.warn("Failed to read file content for validation", e);
            return "Failed to validate file content";
        }
    }

    private String detectContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
