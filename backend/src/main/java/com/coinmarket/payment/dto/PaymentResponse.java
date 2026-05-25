package com.coinmarket.payment.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String transactionNo;
    private String paymentUrl;
    private String status;
    private String errorMessage;
}
