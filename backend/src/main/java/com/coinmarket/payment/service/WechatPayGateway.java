package com.coinmarket.payment.service;

import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class WechatPayGateway implements PaymentGateway {

    @Override
    public String getMethod() {
        return "WECHAT_PAY";
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("WeChat Pay placeholder for order: {}", request.getOrderNo());
        return PaymentResponse.builder()
                .transactionNo("WX" + System.currentTimeMillis())
                .paymentUrl("https://pay.weixin.qq.com/placeholder")
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
