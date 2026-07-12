package com.coinmarket.order.dto;

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
@Schema(description = "订单响应信息")
public class OrderResponse {
    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "买家ID")
    private Long buyerId;

    @Schema(description = "买家名称")
    private String buyerName;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "货币类型")
    private String currency;

    @Schema(description = "收货地址")
    private String shippingAddress;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "配送方式")
    private String shippingMethod;

    @Schema(description = "买家备注")
    private String buyerNote;

    @Schema(description = "支付时间")
    private LocalDateTime paidAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "快递单号")
    private String trackingNumber;

    @Schema(description = "快递公司")
    private String trackingCompany;

    @Schema(description = "订单商品列表")
    private List<OrderItemResponse> items;

    @Schema(description = "订单日志列表")
    private List<OrderLogResponse> logs;

    @Schema(description = "订单编辑日志列表")
    private List<OrderEditLogResponse> editLogs;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
