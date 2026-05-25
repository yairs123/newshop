package com.coinmarket.admin.dto;

import com.coinmarket.admin.entity.News;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NewsResponse {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NewsResponse from(News entity) {
        return NewsResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .content(entity.getContent())
                .imageUrl(entity.getImageUrl())
                .isPublished(entity.getIsPublished())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
