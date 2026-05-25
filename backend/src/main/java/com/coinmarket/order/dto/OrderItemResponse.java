package com.coinmarket.order.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productTitle;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
