package com.coinmarket.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.coinmarket.order.entity.OrderEditLog;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单编辑日志信息")
public class OrderEditLogResponse {
    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "旧值")
    private String oldValue;

    @Schema(description = "新值")
    private String newValue;

    @Schema(description = "修改原因")
    private String reason;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static OrderEditLogResponse from(OrderEditLog log) {
        return OrderEditLogResponse.builder()
                .fieldName(log.getFieldName())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .reason(log.getReason())
                .operator(log.getOperator())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
