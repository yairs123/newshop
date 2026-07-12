package com.coinmarket.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "卖家面板响应")
public class SellerDashboardResponse {
    @Schema(description = "商品数量")
    private long productCount;

    @Schema(description = "待处理订单数")
    private long pendingOrders;

    @Schema(description = "月销售额")
    private BigDecimal monthlySales;

    @Schema(description = "平均评分")
    private double averageRating;

    @Schema(description = "低库存商品（库存<=3）")
    private long lowStockCount;

    @Schema(description = "近期待发货数")
    private long toShipCount;

    @Schema(description = "近期订单（最近5笔）")
    private List<RecentOrderItem> recentOrders;

    @Schema(description = "近7日销售额（逐日）")
    private List<BigDecimal> weeklySales;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "近期订单项")
    public static class RecentOrderItem {
        private Long id;
        private String orderNo;
        private String status;
        private BigDecimal totalAmount;
        private String currency;
        private String buyerName;
        private String createdAt;
    }
}
