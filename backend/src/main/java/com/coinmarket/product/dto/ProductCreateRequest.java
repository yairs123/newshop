package com.coinmarket.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "商品创建请求参数")
public class ProductCreateRequest {
    @NotBlank
    @Size(max = 200)
    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "商品价格")
    private BigDecimal price;

    @NotBlank
    @Schema(description = "货币类型")
    private String currency;

    @Min(0)
    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "评级公司")
    private String ratingCompany;

    @Schema(description = "评级编号")
    private String ratingNumber;

    @Schema(description = "评级等级")
    private String ratingGrade;

    @Schema(description = "发行国家")
    private String country;

    @Schema(description = "发行年份")
    private Integer year;

    @Schema(description = "材质")
    private String material;

    @Schema(description = "面值")
    private String denomination;

    @Schema(description = "重量")
    private BigDecimal weight;

    @Schema(description = "条码")
    private String barcode;

    @Schema(description = "采购价")
    private BigDecimal purchasePrice;

    @Schema(description = "采购货币")
    private String purchaseCurrency;

    @Schema(description = "供应商")
    private String supplier;

    @Schema(description = "来源单据")
    private String sourceInvoice;
}
