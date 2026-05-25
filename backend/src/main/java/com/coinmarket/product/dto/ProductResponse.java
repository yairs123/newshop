package com.coinmarket.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private Long sellerId;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer stock;
    private Long categoryId;
    private String status;
    private String ratingCompany;
    private String ratingNumber;
    private String ratingGrade;
    private String country;
    private Integer year;
    private String material;
    private String denomination;
    private BigDecimal weight;
    private String barcode;
    private Integer viewCount;
    private Integer salesCount;
    private LocalDateTime printedAt;
    private List<String> images;
    private String sellerName;
    private LocalDateTime createdAt;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
    private String supplier;
    private String sourceInvoice;
    private Integer saleQty;
}
