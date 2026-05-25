package com.coinmarket.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reimbursements", schema = "coin_finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reimbursement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "submitter_id")
    private Long submitterId;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
