package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建/编辑银行账户请求")
public class BankAccountRequest {

    @NotBlank
    @Schema(description = "银行名称")
    private String bankName;

    @NotBlank
    @Schema(description = "账户名称")
    private String accountName;

    @NotBlank
    @Schema(description = "账号")
    private String accountNumber;

    @NotBlank
    @Schema(description = "币种")
    private String currency;

    @NotBlank
    @Schema(description = "国家")
    private String country;

    @NotNull
    @Schema(description = "当前余额")
    private BigDecimal currentBalance;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "排序")
    private Integer sortOrder;
}
