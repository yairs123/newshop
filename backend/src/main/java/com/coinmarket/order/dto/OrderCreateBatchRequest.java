package com.coinmarket.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量创建订单请求")
public class OrderCreateBatchRequest {

    @NotEmpty
    @Valid
    @Schema(description = "订单商品列表")
    private List<OrderItemRequest> items;

    @Schema(description = "收货地址")
    private String shippingAddress;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "配送方式")
    private String shippingMethod;

    @Schema(description = "买家备注")
    private String buyerNote;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "订单商品项")
    public static class OrderItemRequest {
        @Schema(description = "商品ID")
        private Long productId;

        @Schema(description = "购买数量")
        private int quantity;
    }
}
