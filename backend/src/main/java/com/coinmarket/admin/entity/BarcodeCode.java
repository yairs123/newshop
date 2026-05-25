package com.coinmarket.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "barcode_codes", schema = "coin_admin")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BarcodeCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_type", nullable = false, length = 20)
    private String codeType;

    @Column(name = "code_value", nullable = false, length = 10)
    private String codeValue;

    @Column(name = "label_en", length = 200)
    private String labelEn;

    @Column(name = "label_zh", length = 200)
    private String labelZh;

    @Column(name = "parent_type", length = 20)
    private String parentType;

    @Column(name = "parent_value", length = 10)
    private String parentValue;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); isActive = true; }
}
