package com.coinmarket.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_logs", schema = "coin_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 20)
    private String toStatus;

    @Column(length = 100)
    private String operator;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
