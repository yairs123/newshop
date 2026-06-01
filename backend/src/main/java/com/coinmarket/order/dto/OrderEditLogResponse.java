package com.coinmarket.order.dto;

import lombok.*;
import com.coinmarket.order.entity.OrderEditLog;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEditLogResponse {
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String reason;
    private String operator;
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
