package com.coinmarket.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images", schema = "coin_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;
}
