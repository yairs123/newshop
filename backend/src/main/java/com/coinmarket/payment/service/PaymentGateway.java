package com.coinmarket.payment.service;

import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;

import java.math.BigDecimal;

public interface PaymentGateway {
    String getMethod();
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse queryPayment(String transactionNo);
    PaymentResponse refund(String transactionNo, BigDecimal amount);
    boolean handleWebhook(String payload, String signature);
}
