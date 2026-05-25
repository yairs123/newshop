package com.coinmarket.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNo;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private String shippingAddress;
    private String paymentMethod;
    private String shippingMethod;
    private String buyerNote;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private List<OrderItemResponse> items;
    private List<OrderLogResponse> logs;
    private LocalDateTime createdAt;
}
