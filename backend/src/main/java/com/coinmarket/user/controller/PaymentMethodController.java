package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.user.dto.PaymentMethodRequest;
import com.coinmarket.user.dto.PaymentMethodResponse;
import com.coinmarket.user.service.PaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Tag(name = "支付方式管理", description = "用户支付方式管理接口")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    @Operation(summary = "获取支付方式列表", description = "获取当前用户的所有支付方式")
    public ApiResponse<List<PaymentMethodResponse>> getPaymentMethods(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(paymentMethodService.getUserPaymentMethods(principal.getId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增支付方式", description = "添加新的支付方式")
    public ApiResponse<PaymentMethodResponse> createPaymentMethod(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody PaymentMethodRequest request) {
        return ApiResponse.success(paymentMethodService.createPaymentMethod(principal.getId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除支付方式", description = "删除指定的支付方式")
    public ApiResponse<Void> deletePaymentMethod(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        paymentMethodService.deletePaymentMethod(id, principal.getId());
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "设为默认支付方式", description = "将指定支付方式设置为默认支付方式")
    public ApiResponse<Void> setDefault(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        paymentMethodService.setDefault(id, principal.getId());
        return ApiResponse.success(null);
    }
}
