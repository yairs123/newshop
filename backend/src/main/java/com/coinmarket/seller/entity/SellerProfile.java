package com.coinmarket.seller.entity;

import com.coinmarket.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seller_profiles", schema = "coin_seller")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfile extends BaseEntity {
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "shop_name", nullable = false, length = 100)
    private String shopName;

    @Column(name = "shop_description", columnDefinition = "TEXT")
    private String shopDescription;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private boolean locked;
}
