package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Schema(description = "利润报表响应数据")
public record ProfitReportResponse(
        @Schema(description = "总收入") BigDecimal totalRevenue,
        @Schema(description = "总成本") BigDecimal totalCost,
        @Schema(description = "净利润") BigDecimal netProfit,
        @Schema(description = "利润率（百分比）") BigDecimal profitMargin,
        @Schema(description = "月度利润明细列表") List<MonthlyProfit> monthlyBreakdown
) {
    @Schema(description = "月度利润数据")
    public record MonthlyProfit(
            @Schema(description = "月份（格式：yyyy-MM）") String month,
            @Schema(description = "月收入") BigDecimal revenue,
            @Schema(description = "月成本") BigDecimal cost,
            @Schema(description = "月利润") BigDecimal profit,
            @Schema(description = "月利润率（百分比）") BigDecimal margin
    ) {}

    public static ProfitReportResponse of(BigDecimal revenue, BigDecimal cost, List<MonthlyProfit> breakdown) {
        BigDecimal netProfit = revenue.subtract(cost);
        BigDecimal margin = revenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        return new ProfitReportResponse(revenue, cost, netProfit, margin.setScale(2, RoundingMode.HALF_UP), breakdown);
    }
}
