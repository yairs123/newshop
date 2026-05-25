package com.coinmarket.user.dto;

import com.coinmarket.user.entity.UserPaymentMethod;
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
public class PaymentMethodResponse {

    private Long id;
    private Long userId;
    private String methodType;
    private String provider;
    private String accountLastFour;
    private String expiryDate;
    private String cardholderName;
    private Boolean isDefault;
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
