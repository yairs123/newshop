package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Schema(description = "销售收入报表响应数据")
public record SalesRevenueResponse(
        @Schema(description = "总销售收入") BigDecimal totalRevenue,
        @Schema(description = "订单数量") Long orderCount,
        @Schema(description = "平均订单金额") BigDecimal averageOrderValue,
        @Schema(description = "月度销售明细列表") List<MonthlySales> monthlyBreakdown
) {
    @Schema(description = "月度销售数据")
    public record MonthlySales(
            @Schema(description = "月份（格式：yyyy-MM）") String month,
            @Schema(description = "月收入") BigDecimal revenue,
            @Schema(description = "月订单数") Long orderCount
    ) {}

    public static SalesRevenueResponse of(BigDecimal totalRevenue, Long orderCount, List<MonthlySales> breakdown) {
        BigDecimal avg = orderCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new SalesRevenueResponse(totalRevenue, orderCount, avg, breakdown);
    }
}
