package com.coinmarket.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "news", schema = "coin_admin")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class News {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_published")
    private Boolean isPublished;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isPublished == null) isPublished = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
