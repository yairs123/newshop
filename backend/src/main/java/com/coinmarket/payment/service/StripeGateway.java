package com.coinmarket.payment.service;

import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class StripeGateway implements PaymentGateway {

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        if (!secretKey.isEmpty()) {
            Stripe.apiKey = secretKey;
        }
    }

    @Override
    public String getMethod() {
        return "CREDIT_CARD";
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(request.getReturnUrl() + "?success=true")
                    .setCancelUrl(request.getReturnUrl() + "?success=false")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency().toLowerCase())
                                                    .setUnitAmount(request.getAmount()
                                                            .multiply(BigDecimal.valueOf(100))
                                                            .longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData
                                                                    .ProductData.builder()
                                                                    .setName("Coin Marketplace Order")
                                                                    .build())
                                                    .build())
                                    .build())
                    .putMetadata("order_no", request.getOrderNo())
                    .build();

            Session session = Session.create(params);
            return PaymentResponse.builder()
                    .paymentUrl(session.getUrl())
                    .transactionNo(session.getId())
                    .status("PENDING")
                    .build();
        } catch (Exception e) {
            log.error("Stripe payment failed", e);
            return PaymentResponse.builder()
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentResponse queryPayment(String transactionNo) {
        try {
            Session session = Session.retrieve(transactionNo);
            return PaymentResponse.builder()
                    .transactionNo(session.getId())
                    .status(session.getPaymentStatus())
                    .build();
        } catch (Exception e) {
            log.error("Stripe query failed", e);
            return PaymentResponse.builder().status("FAILED").errorMessage(e.getMessage()).build();
        }
    }

    @Override
    public PaymentResponse refund(String transactionNo, BigDecimal amount) {
        log.info("Stripe refund placeholder for transaction: {}", transactionNo);
        return PaymentResponse.builder().status("SUCCESS").transactionNo(transactionNo).build();
    }

    @Override
    public boolean handleWebhook(String payload, String signature) {
        log.info("Stripe webhook placeholder");
        return true;
    }
}
