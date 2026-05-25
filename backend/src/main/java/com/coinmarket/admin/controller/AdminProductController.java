package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminProductService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean printed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var responses = adminProductService.listProductsFiltered(status, printed, dateFrom, dateTo, pageable);
        return ApiResponse.success(PageResponse.from(responses));
    }

    @GetMapping("/by-date")
    public ApiResponse<List<ProductResponse>> listByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Boolean printed) {
        var products = adminProductService.listByDateRange(dateFrom, dateTo, printed);
        return ApiResponse.success(products);
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var responses = adminProductService.searchByKeyword(q, pageable);
        return ApiResponse.success(PageResponse.from(responses));
    }

    @GetMapping("/barcode/{barcode}")
    public ApiResponse<ProductResponse> getByBarcode(@PathVariable String barcode) {
        return ApiResponse.success(adminProductService.findByBarcode(barcode));
    }

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(
            @RequestParam Long sellerId,
            @Valid @RequestBody ProductCreateRequest request) {
        var product = adminProductService.createProduct(sellerId, request);
        return ApiResponse.success(product);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateRequest request) {
        var product = adminProductService.updateProduct(id, request);
        return ApiResponse.success(product);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateProductStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        adminProductService.updateProductStatus(id, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/mark-printed")
    public ApiResponse<Void> markAsPrinted(@PathVariable Long id) {
        adminProductService.markAsPrinted(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/mark-printed-batch")
    public ApiResponse<Void> markBatchAsPrinted(@RequestBody List<Long> ids) {
        adminProductService.markBatchAsPrinted(ids);
        return ApiResponse.success(null);
    }
}
