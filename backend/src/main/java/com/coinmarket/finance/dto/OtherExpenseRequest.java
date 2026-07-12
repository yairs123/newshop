package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建其他费用请求")
public class OtherExpenseRequest {

    @NotNull
    @Schema(description = "费用类别ID")
    private Long categoryId;

    @NotNull
    @Positive
    @Schema(description = "金额")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "币种")
    private String currency;

    @NotNull
    @Schema(description = "费用日期")
    private LocalDate expenseDate;

    @NotBlank
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
}
