package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建报销申请请求")
public class ReimbursementRequest {
    @Schema(description = "报销标题")
    private String title;

    @Schema(description = "报销金额")
    private BigDecimal amount;

    @Schema(description = "币种（如 CNY、USD）")
    private String currency;

    @Schema(description = "报销类别")
    private String category;

    @Schema(description = "报销说明")
    private String description;

    @Schema(description = "收据/凭证图片URL")
    private String receiptUrl;
}
