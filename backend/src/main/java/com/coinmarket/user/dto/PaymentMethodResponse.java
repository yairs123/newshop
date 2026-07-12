package com.coinmarket.user.dto;

import com.coinmarket.user.entity.UserPaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "支付方式响应信息")
public class PaymentMethodResponse {

    @Schema(description = "支付方式ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

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

    @Schema(description = "是否默认支付方式")
    private Boolean isDefault;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static PaymentMethodResponse fromEntity(UserPaymentMethod pm) {
        return PaymentMethodResponse.builder()
                .id(pm.getId())
                .userId(pm.getUserId())
                .methodType(pm.getMethodType())
                .provider(pm.getProvider())
                .accountLastFour(pm.getAccountLastFour())
                .expiryDate(pm.getExpiryDate())
                .cardholderName(pm.getCardholderName())
                .isDefault(pm.getIsDefault())
                .createdAt(pm.getCreatedAt())
                .build();
    }
}
