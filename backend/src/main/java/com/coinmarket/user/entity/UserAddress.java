package com.coinmarket.user.entity;

import com.coinmarket.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_addresses", schema = "coin_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
