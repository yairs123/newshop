package com.coinmarket.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_batches", schema = "coin_admin")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryBatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(length = 200)
    private String supplier;

    @Column(name = "invoice_no", length = 100)
    private String invoiceNo;

    @Column(name = "batch_date", nullable = false)
    private LocalDate batchDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "receipt_image", length = 500)
    private String receiptImage;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
