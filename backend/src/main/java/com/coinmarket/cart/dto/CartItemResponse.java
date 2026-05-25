package com.coinmarket.cart.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String title;
    private BigDecimal price;
    private String currency;
    private String image;
    private String country;
    private Integer year;
    private String material;
    private String ratingCompany;
    private String ratingGrade;
    private Integer quantity;
    private Integer stock;
}
