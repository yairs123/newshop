package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.BankAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "银行账户响应数据")
public class BankAccountResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "账户名称")
    private String accountName;

    @Schema(description = "账号")
    private String accountNumber;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "当前余额")
    private BigDecimal currentBalance;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public static BankAccountResponse from(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .bankName(account.getBankName())
                .accountName(account.getAccountName())
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency())
                .country(account.getCountry())
                .currentBalance(account.getCurrentBalance())
                .isActive(account.getIsActive())
                .sortOrder(account.getSortOrder())
                .notes(account.getNotes())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
