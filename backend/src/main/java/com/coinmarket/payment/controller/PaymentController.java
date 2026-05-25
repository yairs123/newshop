package com.coinmarket.payment.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import com.coinmarket.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ApiResponse<PaymentResponse> createPayment(
            @CurrentUser UserPrincipal principal,
            @RequestParam String method,
            @RequestBody PaymentRequest request) {
        return ApiResponse.success(paymentService.createPayment(principal.getId(), method, request));
    }

    @GetMapping("/query")
    public ApiResponse<PaymentResponse> queryPayment(
            @CurrentUser UserPrincipal principal,
            @RequestParam String method,
            @RequestParam String transactionNo) {
        return ApiResponse.success(paymentService.queryPayment(principal.getId(), method, transactionNo));
    }

    @PostMapping("/refund")
    public ApiResponse<PaymentResponse> refund(
            @CurrentUser UserPrincipal principal,
            @RequestParam String method,
            @RequestParam String transactionNo,
            @RequestParam BigDecimal amount) {
        return ApiResponse.success(paymentService.refund(principal.getId(), method, transactionNo, amount));
    }

    @GetMapping("/methods")
    public ApiResponse<List<String>> getAvailableMethods() {
        return ApiResponse.success(paymentService.getAvailableMethods());
    }
}
