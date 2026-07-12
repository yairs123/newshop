package com.coinmarket.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单日志信息")
public class OrderLogResponse {
    @Schema(description = "原状态")
    private String fromStatus;

    @Schema(description = "目标状态")
    private String toStatus;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "备注")
    private String note;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
