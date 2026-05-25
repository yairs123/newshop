package com.coinmarket.order.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLogResponse {
    private String fromStatus;
    private String toStatus;
    private String operator;
    private String note;
    private LocalDateTime createdAt;
}
