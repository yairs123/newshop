package com.coinmarket.payment.service;

import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 银行转账网关（演示模式）。
 * 返回模拟的转账指引链接，与 PayPal/支付宝等网关保持一致的演示行为。
 */
@Slf4j
@Service
public class BankTransferGateway implements PaymentGateway {

    @Override
    public String getMethod() {
        return "BANK_TRANSFER";
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Bank transfer placeholder for order: {}", request.getOrderNo());
        return PaymentResponse.builder()
                .transactionNo("BT" + System.currentTimeMillis())
                .paymentUrl("https://bank.example.com/transfer/placeholder")
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
