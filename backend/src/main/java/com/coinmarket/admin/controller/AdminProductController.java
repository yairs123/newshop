package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminProductService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "后台商品管理", description = "管理员商品管理接口，包括商品查询、创建、更新、上下架、打印标记等操作")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    @Operation(summary = "获取商品列表", description = "分页查询所有商品，支持按状态、打印状态、日期范围过滤")
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
    @Operation(summary = "按日期范围查询", description = "根据日期范围和打印状态查询商品")
    public ApiResponse<List<ProductResponse>> listByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Boolean printed) {
        var products = adminProductService.listByDateRange(dateFrom, dateTo, printed);
        return ApiResponse.success(products);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "根据关键字搜索商品")
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var responses = adminProductService.searchByKeyword(q, pageable);
        return ApiResponse.success(PageResponse.from(responses));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "根据条码查询", description = "根据商品条码获取商品信息")
    public ApiResponse<ProductResponse> getByBarcode(@PathVariable String barcode) {
        return ApiResponse.success(adminProductService.findByBarcode(barcode));
    }

    @PostMapping
    @Operation(summary = "创建商品", description = "为指定卖家创建新商品")
    public ApiResponse<ProductResponse> createProduct(
            @RequestParam Long sellerId,
            @Valid @RequestBody ProductCreateRequest request) {
        var product = adminProductService.createProduct(sellerId, request);
        return ApiResponse.success(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品", description = "更新商品信息")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductCreateRequest request) {
        var product = adminProductService.updateProduct(id, request);
        return ApiResponse.success(product);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态", description = "更新商品上下架状态")
    public ApiResponse<Void> updateProductStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        adminProductService.updateProductStatus(id, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/mark-printed")
    @Operation(summary = "标记已打印", description = "将商品标记为已打印")
    public ApiResponse<Void> markAsPrinted(@PathVariable Long id) {
        adminProductService.markAsPrinted(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/mark-printed-batch")
    @Operation(summary = "批量标记已打印", description = "批量将多个商品标记为已打印")
    public ApiResponse<Void> markBatchAsPrinted(@RequestBody List<Long> ids) {
        adminProductService.markBatchAsPrinted(ids);
        return ApiResponse.success(null);
    }
}
