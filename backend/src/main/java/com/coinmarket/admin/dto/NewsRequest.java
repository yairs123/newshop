package com.coinmarket.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "新闻创建/更新请求参数")
public class NewsRequest {
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
}
