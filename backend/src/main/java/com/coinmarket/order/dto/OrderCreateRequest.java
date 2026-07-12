package com.coinmarket.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建订单请求")
public class OrderCreateRequest {

    @NotNull
    @Schema(description = "商品ID")
    private Long productId;

    @Min(1)
    @Schema(description = "购买数量")
    private int quantity;

    @Schema(description = "收货地址")
    private String shippingAddress;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "配送方式")
    private String shippingMethod;

    @Schema(description = "买家备注")
    private String buyerNote;
}
