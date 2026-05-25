package com.coinmarket.cart.controller;

import com.coinmarket.cart.dto.CartItemRequest;
import com.coinmarket.cart.dto.CartItemResponse;
import com.coinmarket.cart.service.CartService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemResponse>> getCart(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(cartService.getCart(principal.getId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartItemResponse> addItem(@CurrentUser UserPrincipal principal,
                                                  @Valid @RequestBody CartItemRequest request) {
        return ApiResponse.success(cartService.addItem(principal.getId(), request));
    }

    @PutMapping("/{productId}")
    public ApiResponse<Void> updateQuantity(@CurrentUser UserPrincipal principal,
                                            @PathVariable Long productId,
                                            @RequestParam Integer quantity) {
        cartService.updateQuantity(principal.getId(), productId, quantity);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> removeItem(@CurrentUser UserPrincipal principal,
                                        @PathVariable Long productId) {
        cartService.removeItem(principal.getId(), productId);
        return ApiResponse.success(null);
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(@CurrentUser UserPrincipal principal) {
        cartService.clearCart(principal.getId());
        return ApiResponse.success(null);
    }

    @PostMapping("/merge")
    public ApiResponse<Void> mergeCart(@CurrentUser UserPrincipal principal,
                                       @RequestBody List<CartItemRequest> localItems) {
        cartService.mergeCart(principal.getId(), localItems);
        return ApiResponse.success(null);
    }
}
