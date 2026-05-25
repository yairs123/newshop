package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.user.dto.PaymentMethodRequest;
import com.coinmarket.user.dto.PaymentMethodResponse;
import com.coinmarket.user.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public ApiResponse<List<PaymentMethodResponse>> getPaymentMethods(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(paymentMethodService.getUserPaymentMethods(principal.getId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentMethodResponse> createPaymentMethod(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody PaymentMethodRequest request) {
        return ApiResponse.success(paymentMethodService.createPaymentMethod(principal.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePaymentMethod(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        paymentMethodService.deletePaymentMethod(id, principal.getId());
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<Void> setDefault(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        paymentMethodService.setDefault(id, principal.getId());
        return ApiResponse.success(null);
    }
}
