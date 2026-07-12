package com.coinmarket.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "入库记录请求参数")
public class InventoryEntryRequest {
    @Schema(description = "国家编码")
    private String countryCode;

    @Schema(description = "商品条码")
    private String barcode;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "面额编码")
    private String denominationCode;

    @Schema(description = "时期编码")
    private String eraCode;

    @Schema(description = "评级编码")
    private String gradeCode;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "材质")
    private String material;

    @Schema(description = "重量")
    private BigDecimal weight;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "评级公司")
    private String ratingCompany;

    @Schema(description = "评级编号")
    private String ratingNumber;

    @Schema(description = "评级等级")
    private String ratingGrade;

    @Schema(description = "入库数量")
    private Integer quantity;

    @Schema(description = "采购价格")
    private BigDecimal purchasePrice;

    @Schema(description = "货币")
    private String currency;

    @Schema(description = "供应商")
    private String supplier;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "批次日期")
    private LocalDate batchDate;

    @Schema(description = "收据图片URL")
    private String receiptImage;
}
