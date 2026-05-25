package com.coinmarket.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {
    @NotBlank
    @Size(max = 200)
    private String title;

    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotBlank
    private String currency;

    @Min(0)
    private Integer stock;

    private Long categoryId;

    private String ratingCompany;
    private String ratingNumber;
    private String ratingGrade;

    private String country;
    private Integer year;
    private String material;
    private String denomination;
    private BigDecimal weight;
    private String barcode;
}
