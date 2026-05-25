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
public class AddressRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "City is required")
    private String city;

    private String zipCode;

    @NotBlank(message = "Address is required")
    private String address;

    private Boolean isDefault;
}
