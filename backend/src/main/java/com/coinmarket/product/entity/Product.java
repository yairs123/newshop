package com.coinmarket.product.entity;

import com.coinmarket.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products", schema = "coin_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "rating_company", length = 20)
    private String ratingCompany;

    @Column(name = "rating_number", length = 50)
    private String ratingNumber;

    @Column(name = "rating_grade", length = 50)
    private String ratingGrade;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_currency", length = 3)
    private String purchaseCurrency;

    @Column(length = 200)
    private String supplier;

    @Column(name = "source_invoice", length = 100)
    private String sourceInvoice;

    @Column(name = "sale_qty")
    private Integer saleQty;

    @Column(length = 100)
    private String country;

    @Column(name = "\"year\"")
    private Integer year;

    @Column(length = 50)
    private String material;

    @Column(length = 50)
    private String denomination;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(length = 50, unique = true)
    private String barcode;

    @Column(name = "printed_at")
    private LocalDateTime printedAt;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "sales_count", nullable = false)
    private Integer salesCount;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images;
}
