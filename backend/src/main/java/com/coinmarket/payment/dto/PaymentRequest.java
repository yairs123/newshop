package com.coinmarket.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付请求")
public class PaymentRequest {
    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "货币类型")
    private String currency;

    @Schema(description = "支付成功返回URL")
    private String returnUrl;

    @Schema(description = "支付取消返回URL")
    private String cancelUrl;

    @Schema(description = "支付描述")
    private String description;
}
