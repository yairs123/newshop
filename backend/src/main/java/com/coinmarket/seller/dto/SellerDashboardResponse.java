package com.coinmarket.seller.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerDashboardResponse {
    private long productCount;
    private long pendingOrders;
    private BigDecimal monthlySales;
    private double averageRating;
}
