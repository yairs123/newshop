package com.coinmarket.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "购物车商品响应信息")
public class CartItemResponse {
    @Schema(description = "购物车项ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品价格")
    private BigDecimal price;

    @Schema(description = "货币类型")
    private String currency;

    @Schema(description = "商品图片")
    private String image;

    @Schema(description = "发行国家")
    private String country;

    @Schema(description = "发行年份")
    private Integer year;

    @Schema(description = "材质")
    private String material;

    @Schema(description = "评级公司")
    private String ratingCompany;

    @Schema(description = "评级等级")
    private String ratingGrade;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "库存")
    private Integer stock;
}
