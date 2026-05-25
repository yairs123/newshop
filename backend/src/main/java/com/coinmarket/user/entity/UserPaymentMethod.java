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
@Table(name = "user_payment_methods", schema = "coin_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPaymentMethod extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "method_type", nullable = false, length = 30)
    private String methodType;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "account_last_four", length = 4)
    private String accountLastFour;

    @Column(name = "expiry_date", length = 7)
    private String expiryDate;

    @Column(name = "cardholder_name", length = 100)
    private String cardholderName;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
