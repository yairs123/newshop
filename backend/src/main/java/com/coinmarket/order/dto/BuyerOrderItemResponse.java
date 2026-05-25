package com.coinmarket.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerOrderItemResponse {
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal unitPrice;
    private LocalDateTime lastPurchasedAt;
    private Long orderId;
}
