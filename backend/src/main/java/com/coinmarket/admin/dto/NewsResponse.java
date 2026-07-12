package com.coinmarket.admin.dto;

import com.coinmarket.admin.entity.News;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "新闻响应信息")
public class NewsResponse {
    @Schema(description = "新闻ID")
    private Long id;

    @Schema(description = "新闻标题")
    private String title;

    @Schema(description = "新闻摘要")
    private String summary;

    @Schema(description = "新闻内容")
    private String content;

    @Schema(description = "新闻图片URL")
    private String imageUrl;

    @Schema(description = "是否发布")
    private Boolean isPublished;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
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
