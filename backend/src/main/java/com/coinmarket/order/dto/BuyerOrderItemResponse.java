package com.coinmarket.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "买家购买商品响应信息")
public class BuyerOrderItemResponse {
    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "最近购买时间")
    private LocalDateTime lastPurchasedAt;

    @Schema(description = "订单ID")
    private Long orderId;
}
