package com.coinmarket.product.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.product.dto.ReviewRequest;
import com.coinmarket.product.dto.ReviewResponse;
import com.coinmarket.product.dto.ReviewSummary;
import com.coinmarket.product.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "商品评价", description = "商品评价管理接口")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建评价", description = "对已购买的商品进行评价")
    public ApiResponse<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(reviewService.createReview(principal.getId(), request));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "获取商品评价列表", description = "根据商品ID获取所有评价")
    public ApiResponse<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        return ApiResponse.success(reviewService.getProductReviews(productId));
    }

    @GetMapping("/product/{productId}/summary")
    @Operation(summary = "获取商品评价汇总", description = "获取商品的平均评分和评价总数")
    public ApiResponse<ReviewSummary> getProductSummary(@PathVariable Long productId) {
        return ApiResponse.success(reviewService.getProductSummary(productId));
    }

    @GetMapping("/check")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查是否已评价", description = "检查当前用户是否已评价指定商品")
    public ApiResponse<Boolean> checkReviewed(
            @RequestParam Long productId,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(reviewService.hasReviewed(principal.getId(), productId));
    }
}
