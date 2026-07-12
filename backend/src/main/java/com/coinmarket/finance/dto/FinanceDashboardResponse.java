package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "财务仪表盘响应数据")
public class FinanceDashboardResponse {
    @Schema(description = "总采购成本")
    private BigDecimal totalPurchaseCost;

    @Schema(description = "总销售收入")
    private BigDecimal totalSalesRevenue;

    @Schema(description = "净利润")
    private BigDecimal netProfit;

    @Schema(description = "利润率（百分比）")
    private BigDecimal profitMargin;

    @Schema(description = "待处理报销数")
    private long pendingReimbursements;

    @Schema(description = "总报销数")
    private long totalReimbursements;
}
