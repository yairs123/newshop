package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "财务总览响应数据")
public class FinanceOverviewResponse {

    @Schema(description = "总收入")
    private BigDecimal totalIncome;

    @Schema(description = "总支出")
    private BigDecimal totalExpenses;

    @Schema(description = "净利润")
    private BigDecimal netProfit;

    @Schema(description = "利润率")
    private BigDecimal profitMargin;

    @Schema(description = "银行总余额")
    private BigDecimal totalBankBalance;

    @Schema(description = "月度趋势")
    private List<MonthlyTrend> monthlyTrend;

    @Schema(description = "费用分类")
    private List<ExpenseBreakdown> expenseBreakdown;

    @Schema(description = "银行余额明细")
    private List<BankBalanceInfo> bankBreakdown;

    @Schema(description = "月度趋势记录")
    public record MonthlyTrend(String month, BigDecimal income, BigDecimal expenses, BigDecimal profit) {}

    @Schema(description = "费用分类明细")
    public record ExpenseBreakdown(String category, BigDecimal amount, Double percentage) {}

    @Schema(description = "银行余额信息")
    public record BankBalanceInfo(String bankName, BigDecimal balance, String currency) {}
}
