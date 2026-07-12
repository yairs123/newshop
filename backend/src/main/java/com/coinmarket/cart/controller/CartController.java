package com.coinmarket.cart.controller;

import com.coinmarket.cart.dto.CartItemRequest;
import com.coinmarket.cart.dto.CartItemResponse;
import com.coinmarket.cart.service.CartService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "购物车管理", description = "购物车商品的增删改查以及合并操作")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "查看购物车", description = "获取当前用户的购物车商品列表")
    public ApiResponse<List<CartItemResponse>> getCart(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(cartService.getCart(principal.getId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "添加商品到购物车", description = "将指定商品添加到购物车")
    public ApiResponse<CartItemResponse> addItem(@CurrentUser UserPrincipal principal,
                                                  @Valid @RequestBody CartItemRequest request) {
        return ApiResponse.success(cartService.addItem(principal.getId(), request));
    }

    @PutMapping("/{productId}")
    @Operation(summary = "更新商品数量", description = "修改购物车中指定商品的数量")
    public ApiResponse<Void> updateQuantity(@CurrentUser UserPrincipal principal,
                                            @PathVariable Long productId,
                                            @RequestParam Integer quantity) {
        cartService.updateQuantity(principal.getId(), productId, quantity);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "删除购物车商品", description = "从购物车中移除指定商品")
    public ApiResponse<Void> removeItem(@CurrentUser UserPrincipal principal,
                                        @PathVariable Long productId) {
        cartService.removeItem(principal.getId(), productId);
        return ApiResponse.success(null);
    }

    @DeleteMapping
    @Operation(summary = "清空购物车", description = "清空当前用户的购物车中的所有商品")
    public ApiResponse<Void> clearCart(@CurrentUser UserPrincipal principal) {
        cartService.clearCart(principal.getId());
        return ApiResponse.success(null);
    }

    @PostMapping("/merge")
    @Operation(summary = "合并购物车", description = "将本地购物车数据合并到服务端购物车")
    public ApiResponse<Void> mergeCart(@CurrentUser UserPrincipal principal,
                                       @RequestBody List<CartItemRequest> localItems) {
        cartService.mergeCart(principal.getId(), localItems);
        return ApiResponse.success(null);
    }
}
