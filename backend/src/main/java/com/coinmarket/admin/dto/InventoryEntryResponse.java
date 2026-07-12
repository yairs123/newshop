package com.coinmarket.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "入库记录响应信息")
public class InventoryEntryResponse {
    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "条码")
    private String barcode;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "面额")
    private String denomination;

    @Schema(description = "评级")
    private String grade;

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

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
