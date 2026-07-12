package com.coinmarket.payment.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import com.coinmarket.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "支付管理", description = "支付创建、查询、退款等操作")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "创建支付", description = "创建一笔新的支付交易")
    public ApiResponse<PaymentResponse> createPayment(
            @CurrentUser UserPrincipal principal,
            @RequestParam String method,
            @RequestBody PaymentRequest request) {
        return ApiResponse.success(paymentService.createPayment(principal.getId(), method, request));
    }

    @GetMapping("/query")
    @Operation(summary = "查询支付", description = "根据交易号查询支付状态和详情")
    public ApiResponse<PaymentResponse> queryPayment(
            @CurrentUser UserPrincipal principal,
            @RequestParam String method,
            @RequestParam String transactionNo) {
        return ApiResponse.success(paymentService.queryPayment(principal.getId(), method, transactionNo));
    }

    @PostMapping("/refund")
    @Operation(summary = "退款", description = "对指定交易发起退款")
    public ApiResponse<PaymentResponse> refund(
            @CurrentUser UserPrincipal principal,
            @RequestParam String method,
            @RequestParam String transactionNo,
            @RequestParam BigDecimal amount) {
        return ApiResponse.success(paymentService.refund(principal.getId(), method, transactionNo, amount));
    }

    @GetMapping("/methods")
    @Operation(summary = "获取支付方式", description = "查询系统支持的支付方式列表")
    public ApiResponse<List<String>> getAvailableMethods() {
        return ApiResponse.success(paymentService.getAvailableMethods());
    }
}
