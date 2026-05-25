package com.coinmarket.payment.service;

import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PaypalGateway implements PaymentGateway {

    @Override
    public String getMethod() {
        return "PAYPAL";
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("PayPal placeholder for order: {}", request.getOrderNo());
        return PaymentResponse.builder()
                .transactionNo("PP" + System.currentTimeMillis())
                .paymentUrl("https://www.paypal.com/checkout/placeholder")
                .status("PENDING")
                .build();
    }

    @Override
    public PaymentResponse queryPayment(String transactionNo) {
        return PaymentResponse.builder().transactionNo(transactionNo).status("SUCCESS").build();
    }

    @Override
    public PaymentResponse refund(String transactionNo, BigDecimal amount) {
        return PaymentResponse.builder().transactionNo(transactionNo).status("SUCCESS").build();
    }

    @Override
    public boolean handleWebhook(String payload, String signature) {
        return true;
    }
}
