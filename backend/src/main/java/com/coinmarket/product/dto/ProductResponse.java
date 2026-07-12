package com.coinmarket.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品响应信息")
public class ProductResponse {
    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "货币类型")
    private String currency;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "商品状态")
    private String status;

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

    @Schema(description = "浏览数量")
    private Integer viewCount;

    @Schema(description = "销售数量")
    private Integer salesCount;

    @Schema(description = "印刷时间")
    private LocalDateTime printedAt;

    @Schema(description = "商品图片列表")
    private List<String> images;

    @Schema(description = "卖家名称")
    private String sellerName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "采购价格")
    private BigDecimal purchasePrice;

    @Schema(description = "采购货币")
    private String purchaseCurrency;

    @Schema(description = "供应商")
    private String supplier;

    @Schema(description = "采购发票")
    private String sourceInvoice;

    @Schema(description = "出售数量")
    private Integer saleQty;
}
