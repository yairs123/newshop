package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.OtherExpense;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "其他费用响应数据")
public class OtherExpenseResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "费用类别ID")
    private Long categoryId;

    @Schema(description = "费用类别名称")
    private String categoryName;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "费用日期")
    private LocalDate expenseDate;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "供应商")
    private String vendor;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "银行账户ID")
    private Long bankAccountId;

    @Schema(description = "收据URL")
    private String receiptUrl;

    @Schema(description = "创建人")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static OtherExpenseResponse from(OtherExpense e) {
        return OtherExpenseResponse.builder()
                .id(e.getId())
                .categoryId(e.getCategoryId())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .expenseDate(e.getExpenseDate())
                .description(e.getDescription())
                .vendor(e.getVendor())
                .paymentMethod(e.getPaymentMethod())
                .bankAccountId(e.getBankAccountId())
                .receiptUrl(e.getReceiptUrl())
                .createdBy(e.getCreatedBy())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public OtherExpenseResponse withCategoryName(String name) {
        this.categoryName = name;
        return this;
    }
}
