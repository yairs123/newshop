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
import com.coinmarket.product.repository.CategoryRepository;
import com.coinmarket.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String ratingCompany,
            @RequestParam(required = false) String ratingGrade,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> result = productService.searchProducts(
                keyword, categoryId, ratingCompany, ratingGrade,
                minPrice, maxPrice, country, year, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
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
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getProduct(id));
    }

    @GetMapping("/{id}/related")
    public ApiResponse<List<ProductResponse>> getRelatedProducts(@PathVariable Long id) {
        return ApiResponse.success(productService.getRelatedProducts(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponse> createProduct(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success(productService.createProduct(principal.getId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success(productService.updateProduct(id, principal.getId(), request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateProductStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        productService.setProductStatus(id, status);
        return ApiResponse.success(null);
    }
}
