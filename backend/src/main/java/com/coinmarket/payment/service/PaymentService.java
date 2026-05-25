package com.coinmarket.payment.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final Map<String, PaymentGateway> gatewayMap;

    public PaymentService(List<PaymentGateway> gateways) {
        this.gatewayMap = gateways.stream()
                .collect(Collectors.toMap(PaymentGateway::getMethod, g -> g));
    }

    public PaymentResponse createPayment(Long userId, String method, PaymentRequest request) {
        PaymentGateway gateway = gatewayMap.get(method.toUpperCase());
        if (gateway == null) {
            throw new BusinessException("不支持的支付方式: " + method);
        }
        return gateway.createPayment(request);
    }

    public PaymentResponse queryPayment(Long userId, String method, String transactionNo) {
        PaymentGateway gateway = gatewayMap.get(method.toUpperCase());
        if (gateway == null) {
            throw new BusinessException("不支持的支付方式: " + method);
        }
        return gateway.queryPayment(transactionNo);
    }

    public PaymentResponse refund(Long userId, String method, String transactionNo, BigDecimal amount) {
        PaymentGateway gateway = gatewayMap.get(method.toUpperCase());
        if (gateway == null) {
            throw new BusinessException("不支持的支付方式: " + method);
        }
        return gateway.refund(transactionNo, amount);
    }

    public List<String> getAvailableMethods() {
        return List.copyOf(gatewayMap.keySet());
    }
}
