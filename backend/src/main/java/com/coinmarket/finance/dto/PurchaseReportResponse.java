package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Schema(description = "采购报表响应数据")
public record PurchaseReportResponse(
        @Schema(description = "总采购成本") BigDecimal totalPurchaseCost,
        @Schema(description = "采购批次数量") Long batchCount,
        @Schema(description = "每批次平均成本") BigDecimal averageCostPerBatch,
        @Schema(description = "月度采购明细列表") List<MonthlyPurchase> monthlyBreakdown
) {
    @Schema(description = "月度采购数据")
    public record MonthlyPurchase(
            @Schema(description = "月份（格式：yyyy-MM）") String month,
            @Schema(description = "当月采购成本") BigDecimal totalCost,
            @Schema(description = "当月批次数量") Long batchCount
    ) {}

    public static PurchaseReportResponse of(BigDecimal totalCost, Long batchCount, List<MonthlyPurchase> breakdown) {
        BigDecimal avg = batchCount > 0
                ? totalCost.divide(BigDecimal.valueOf(batchCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new PurchaseReportResponse(totalCost, batchCount, avg, breakdown);
    }
}
