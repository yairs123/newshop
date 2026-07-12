package com.coinmarket.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "支付方式请求参数")
public class PaymentMethodRequest {

    @NotBlank(message = "Method type is required")
    @Schema(description = "支付方式类型")
    private String methodType;

    @Schema(description = "支付提供商")
    private String provider;

    @Schema(description = "账户后四位")
    private String accountLastFour;

    @Schema(description = "有效期")
    private String expiryDate;

    @Schema(description = "持卡人姓名")
    private String cardholderName;

    @Schema(description = "是否设为默认")
    private Boolean isDefault;
}
