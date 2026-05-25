package com.coinmarket.common.storage;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.util.BarcodeUtil;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.entity.ProductImage;
import com.coinmarket.product.repository.ProductImageRepository;
import com.coinmarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
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
public class FileController {

    private final FileStorageService storageService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * Upload a single image for a product identified by barcode.
     */
    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(
            @RequestParam String barcode,
            @RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("File is empty");
        }
        if (!BarcodeUtil.isValid(barcode)) {
            return ApiResponse.error("Invalid barcode: " + barcode);
        }
        var productOpt = productRepository.findByBarcode(barcode);
        if (productOpt.isEmpty()) {
            return ApiResponse.error("No product found with barcode: " + barcode);
        }

        try {
            String filename = sanitizeFilename(file.getOriginalFilename(), barcode);
            String path = storageService.store(barcode, filename, file.getBytes());

            // Determine sort order
            Product product = productOpt.get();
            int nextOrder = product.getImages() != null ? product.getImages().size() + 1 : 1;

            ProductImage image = ProductImage.builder()
                    .product(product)
                    .url("/api/files/" + path)
                    .sortOrder(nextOrder)
                    .isPrimary(nextOrder == 1)
                    .build();
            productImageRepository.save(image);

            return ApiResponse.success(Map.of(
                    "path", path,
                    "url", "/api/files/" + path,
                    "barcode", barcode
            ));
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
            var productOpt = productRepository.findByBarcode(barcode);
            if (productOpt.isEmpty()) {
                failed.add(Map.of("file", originalName, "reason", "No product found for barcode: " + barcode));
                continue;
            }

            try {
                String filename = sanitizeFilename(originalName, barcode);
                String path = storageService.store(barcode, filename, file.getBytes());

                Product product = productOpt.get();
                int nextOrder = product.getImages() != null ? product.getImages().size() + 1 : 1;

                ProductImage image = ProductImage.builder()
                        .product(product)
                        .url("/api/files/" + path)
                        .sortOrder(nextOrder)
                        .isPrimary(nextOrder == 1)
                        .build();
                productImageRepository.save(image);

                succeeded.add(Map.of(
                        "file", originalName,
                        "path", path,
                        "barcode", barcode
                ));
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
     * Serve stored files.
     */
    @GetMapping("/{barcode}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String barcode,
            @PathVariable String filename) {
        if (!(storageService instanceof LocalFileStorageService local)) {
            return ResponseEntity.notFound().build();
        }
        String relativePath = barcode + "/" + filename;
        Resource resource = local.loadAsResource(relativePath);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        String contentType = detectContentType(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .body(resource);
    }

    /**
     * List files for a given barcode.
     */
    @GetMapping("/{barcode}")
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
