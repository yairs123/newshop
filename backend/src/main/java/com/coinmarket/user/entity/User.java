package com.coinmarket.user.entity;

import com.coinmarket.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users", schema = "coin_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "phone_country_code", length = 5)
    private String phoneCountryCode;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        schema = "coin_users",
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
