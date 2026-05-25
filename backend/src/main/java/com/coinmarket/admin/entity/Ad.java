package com.coinmarket.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ads", schema = "coin_admin")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (sortOrder == null) sortOrder = 0;
    }
}
