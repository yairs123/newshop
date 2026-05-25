package com.coinmarket.finance.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record ProfitReportResponse(
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal netProfit,
        BigDecimal profitMargin,
        List<MonthlyProfit> monthlyBreakdown
) {
    public record MonthlyProfit(String month, BigDecimal revenue, BigDecimal cost, BigDecimal profit, BigDecimal margin) {}

    public static ProfitReportResponse of(BigDecimal revenue, BigDecimal cost, List<MonthlyProfit> breakdown) {
        BigDecimal netProfit = revenue.subtract(cost);
        BigDecimal margin = revenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        return new ProfitReportResponse(revenue, cost, netProfit, margin.setScale(2, RoundingMode.HALF_UP), breakdown);
    }
}
