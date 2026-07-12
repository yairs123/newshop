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
@Schema(description = "地址请求参数")
public class AddressRequest {

    @NotBlank(message = "Full name is required")
    @Schema(description = "收件人姓名")
    private String fullName;

    @Schema(description = "联系电话")
    private String phone;

    @NotBlank(message = "Country is required")
    @Schema(description = "国家")
    private String country;

    @NotBlank(message = "City is required")
    @Schema(description = "城市")
    private String city;

    @Schema(description = "邮政编码")
    private String zipCode;

    @NotBlank(message = "Address is required")
    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "是否设为默认地址")
    private Boolean isDefault;
}
