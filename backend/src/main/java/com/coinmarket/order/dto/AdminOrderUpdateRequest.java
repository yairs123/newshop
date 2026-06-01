package com.coinmarket.order.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderUpdateRequest {
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private String trackingNumber;
    private String trackingCompany;
    private String reason;
}
