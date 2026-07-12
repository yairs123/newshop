package com.coinmarket.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "管理员更新订单请求")
public class AdminOrderUpdateRequest {
    @Schema(description = "支付时间")
    private LocalDateTime paidAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "快递单号")
    private String trackingNumber;

    @Schema(description = "快递公司")
    private String trackingCompany;

    @Schema(description = "修改原因")
    private String reason;
}
