package com.coinmarket.finance.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record SalesRevenueResponse(
        BigDecimal totalRevenue,
        Long orderCount,
        BigDecimal averageOrderValue,
        List<MonthlySales> monthlyBreakdown
) {
    public record MonthlySales(String month, BigDecimal revenue, Long orderCount) {}

    public static SalesRevenueResponse of(BigDecimal totalRevenue, Long orderCount, List<MonthlySales> breakdown) {
        BigDecimal avg = orderCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new SalesRevenueResponse(totalRevenue, orderCount, avg, breakdown);
    }
}
