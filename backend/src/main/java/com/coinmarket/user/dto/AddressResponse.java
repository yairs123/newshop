package com.coinmarket.user.dto;

import com.coinmarket.user.entity.UserAddress;
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
public class AddressResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String country;
    private String city;
    private String zipCode;
    private String address;
    private Boolean isDefault;
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
