package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建银行转账请求")
public class BankTransferRequest {

    @NotNull
    @Schema(description = "转出账户ID")
    private Long fromAccountId;

    @NotNull
    @Schema(description = "转入账户ID")
    private Long toAccountId;

    @NotNull
    @Positive
    @Schema(description = "转账金额")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "币种")
    private String currency;

    @Schema(description = "手续费")
    private BigDecimal fee;

    @NotNull
    @Schema(description = "转账日期")
    private LocalDate transferDate;

    @Schema(description = "参考编号")
    private String referenceNo;

    @Schema(description = "描述")
    private String description;
}
