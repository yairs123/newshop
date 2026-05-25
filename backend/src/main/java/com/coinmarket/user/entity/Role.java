package com.coinmarket.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles", schema = "coin_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;
}
