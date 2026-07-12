package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.BankTransfer;
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
@Schema(description = "银行转账响应数据")
public class BankTransferResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "转出账户ID")
    private Long fromAccountId;

    @Schema(description = "转入账户ID")
    private Long toAccountId;

    @Schema(description = "转账金额")
    private BigDecimal amount;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "手续费")
    private BigDecimal fee;

    @Schema(description = "转账日期")
    private LocalDate transferDate;

    @Schema(description = "参考编号")
    private String referenceNo;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建人")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "转出银行名称")
    private String fromBankName;

    @Schema(description = "转入银行名称")
    private String toBankName;

    public static BankTransferResponse from(BankTransfer transfer) {
        return BankTransferResponse.builder()
                .id(transfer.getId())
                .fromAccountId(transfer.getFromAccountId())
                .toAccountId(transfer.getToAccountId())
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency())
                .fee(transfer.getFee())
                .transferDate(transfer.getTransferDate())
                .referenceNo(transfer.getReferenceNo())
                .description(transfer.getDescription())
                .createdBy(transfer.getCreatedBy())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}
