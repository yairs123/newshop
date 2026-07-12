package com.coinmarket.user.dto;

import com.coinmarket.user.entity.UserAddress;
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
@Schema(description = "地址响应信息")
public class AddressResponse {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "收件人姓名")
    private String fullName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "邮政编码")
    private String zipCode;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static AddressResponse fromEntity(UserAddress addr) {
        return AddressResponse.builder()
                .id(addr.getId())
                .userId(addr.getUserId())
                .fullName(addr.getFullName())
                .phone(addr.getPhone())
                .country(addr.getCountry())
                .city(addr.getCity())
                .zipCode(addr.getZipCode())
                .address(addr.getAddress())
                .isDefault(addr.getIsDefault())
                .createdAt(addr.getCreatedAt())
                .build();
    }
}
