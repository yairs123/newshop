package com.coinmarket.order.entity;

import com.coinmarket.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders", schema = "coin_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true, length = 30)
    private String orderNo;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "shipping_country", length = 100)
    private String shippingCountry;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "shipping_method", length = 30)
    private String shippingMethod;

    @Column(name = "buyer_note", columnDefinition = "TEXT")
    private String buyerNote;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;
}
