package com.coinmarket.finance.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FinanceDashboardResponse {
    private BigDecimal totalPurchaseCost;
    private BigDecimal totalSalesRevenue;
    private BigDecimal netProfit;
    private BigDecimal profitMargin;
    private long pendingReimbursements;
    private long totalReimbursements;
}
