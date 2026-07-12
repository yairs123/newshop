package com.coinmarket.product.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import java.util.List;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.product.dto.CategoryResponse;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Category;
import com.coinmarket.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品信息管理接口")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "搜索商品", description = "根据关键词、分类、价格等条件搜索商品")
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String ratingCompany,
            @RequestParam(required = false) String ratingGrade,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable;
        if (sort != null && !sort.isBlank()) {
            var parts = sort.split(",");
            var dir = parts.length > 1 && parts[1].equalsIgnoreCase("desc")
                    ? org.springframework.data.domain.Sort.Direction.DESC
                    : org.springframework.data.domain.Sort.Direction.ASC;
            pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(dir, parts[0]));
        } else {
            pageable = PageRequest.of(page, size);
        }
        Page<ProductResponse> result = productService.searchProducts(
                keyword, categoryId, ratingCompany, ratingGrade,
                minPrice, maxPrice, country, year, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping("/categories")
    @Operation(summary = "获取分类列表", description = "获取所有商品分类")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        List<Category> categories = productService.getCategories();
        List<CategoryResponse> result = categories.stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .slug(c.getSlug())
                        .build())
                .toList();
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "根据ID获取商品详细信息")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getProduct(id));
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "获取相关商品", description = "获取指定商品的相关商品推荐")
    public ApiResponse<List<ProductResponse>> getRelatedProducts(@PathVariable Long id) {
        return ApiResponse.success(productService.getRelatedProducts(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建商品", description = "创建新的商品信息")
    public ApiResponse<ProductResponse> createProduct(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success(productService.createProduct(principal.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新商品", description = "更新指定的商品信息")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success(productService.updateProduct(id, principal.getId(), request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新商品状态", description = "更新商品的上架或下架状态")
    public ApiResponse<Void> updateProductStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        productService.setProductStatus(id, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/copy")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "复制商品", description = "复制指定商品为新的商品")
    public ApiResponse<ProductResponse> copyProduct(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(productService.copyProduct(id, principal.getId()));
    }

    @PutMapping("/batch-status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "批量更新状态", description = "批量上架或下架商品")
    public ApiResponse<Void> batchUpdateStatus(
            @RequestBody Map<String, Object> body,
            @CurrentUser UserPrincipal principal) {
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("ids");
        List<Long> ids = idsRaw.stream().map(Long::valueOf).toList();
        String status = (String) body.get("status");
        productService.batchUpdateStatus(ids, status, principal.getId());
        return ApiResponse.success(null);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的商品", description = "获取当前卖家的商品列表（分页）")
    public ApiResponse<PageResponse<ProductResponse>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @CurrentUser UserPrincipal principal) {
        var pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        var result = productService.listSellerProducts(principal.getId(), pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @PostMapping("/{id}/mark-printed")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "标记已打印", description = "将商品标记为条码已打印")
    public ApiResponse<ProductResponse> markAsPrinted(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(productService.markAsPrinted(id, principal.getId()));
    }

    @PostMapping("/mark-printed-batch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "批量标记已打印", description = "批量将多个商品标记为条码已打印")
    public ApiResponse<Void> batchMarkAsPrinted(
            @RequestBody Map<String, Object> body,
            @CurrentUser UserPrincipal principal) {
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("ids");
        List<Long> ids = idsRaw.stream().map(Long::valueOf).toList();
        productService.batchMarkAsPrinted(ids, principal.getId());
        return ApiResponse.success(null);
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "根据条码查商品", description = "通过条码查询商品信息")
    public ApiResponse<ProductResponse> getByBarcode(@PathVariable String barcode) {
        return ApiResponse.success(productService.findByBarcode(barcode));
    }
}
