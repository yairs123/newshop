package com.coinmarket.payment.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String orderNo;
    private BigDecimal amount;
    private String currency;
    private String returnUrl;
    private String cancelUrl;
    private String description;
}
