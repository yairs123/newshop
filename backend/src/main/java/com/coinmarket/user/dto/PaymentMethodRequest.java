package com.coinmarket.user.dto;

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
public class PaymentMethodRequest {

    @NotBlank(message = "Method type is required")
    private String methodType;

    private String provider;

    private String accountLastFour;

    private String expiryDate;

    private String cardholderName;

    private Boolean isDefault;
}
