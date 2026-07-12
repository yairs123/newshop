package com.coinmarket.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单商品响应信息")
public class OrderItemResponse {
    @Schema(description = "订单商品ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "数量")
    private int quantity;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "小计金额")
    private BigDecimal subtotal;
}
