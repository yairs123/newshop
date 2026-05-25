package com.coinmarket.finance.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record PurchaseReportResponse(
        BigDecimal totalPurchaseCost,
        Long batchCount,
        BigDecimal averageCostPerBatch,
        List<MonthlyPurchase> monthlyBreakdown
) {
    public record MonthlyPurchase(String month, BigDecimal totalCost, Long batchCount) {}

    public static PurchaseReportResponse of(BigDecimal totalCost, Long batchCount, List<MonthlyPurchase> breakdown) {
        BigDecimal avg = batchCount > 0
                ? totalCost.divide(BigDecimal.valueOf(batchCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new PurchaseReportResponse(totalCost, batchCount, avg, breakdown);
    }
}
