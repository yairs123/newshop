package com.coinmarket.common.storage;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.storage.FileStorageService.StoreResult;
import com.coinmarket.common.util.BarcodeUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    /**
     * Upload a single image for a product identified by barcode.
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传商品图片")
    public ApiResponse<Map<String, String>> upload(
            @RequestParam String barcode,
            @RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("File is empty");
        }
        if (!BarcodeUtil.isValid(barcode)) {
            return ApiResponse.error("Invalid barcode: " + barcode);
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
    @Operation(summary = "通用上传", description = "上传通用文件（广告图片等），返回文件URL")
    public ApiResponse<Map<String, String>> uploadGeneral(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("File is empty");
        }
        try {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
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

    private String detectContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
