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
@Schema(description = "创建人员费用请求")
public class PersonnelExpenseRequest {

    @NotBlank
    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "职位")
    private String position;

    @NotNull
    @Positive
    @Schema(description = "金额")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "币种")
    private String currency;

    @NotNull
    @Schema(description = "支付日期")
    private LocalDate payDate;

    @NotNull
    @Schema(description = "期间开始")
    private LocalDate periodStart;

    @NotNull
    @Schema(description = "期间结束")
    private LocalDate periodEnd;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "银行账户ID")
    private Long bankAccountId;
}
