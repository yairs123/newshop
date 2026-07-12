package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.PersonnelExpense;
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
@Schema(description = "人员费用响应数据")
public class PersonnelExpenseResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "支付日期")
    private LocalDate payDate;

    @Schema(description = "期间开始")
    private LocalDate periodStart;

    @Schema(description = "期间结束")
    private LocalDate periodEnd;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "银行账户ID")
    private Long bankAccountId;

    @Schema(description = "创建人")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static PersonnelExpenseResponse from(PersonnelExpense p) {
        return PersonnelExpenseResponse.builder()
                .id(p.getId())
                .employeeName(p.getEmployeeName())
                .position(p.getPosition())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .payDate(p.getPayDate())
                .periodStart(p.getPeriodStart())
                .periodEnd(p.getPeriodEnd())
                .notes(p.getNotes())
                .paymentMethod(p.getPaymentMethod())
                .bankAccountId(p.getBankAccountId())
                .createdBy(p.getCreatedBy())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
