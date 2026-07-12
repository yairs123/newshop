package com.coinmarket.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付响应信息")
public class PaymentResponse {
    @Schema(description = "交易号")
    private String transactionNo;

    @Schema(description = "支付链接")
    private String paymentUrl;

    @Schema(description = "支付状态")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;
}
