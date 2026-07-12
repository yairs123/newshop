package com.coinmarket.admin.dto;

import com.coinmarket.admin.entity.Ad;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "广告响应信息")
public class AdResponse {
    @Schema(description = "广告ID")
    private Long id;

    @Schema(description = "广告标题")
    private String title;

    @Schema(description = "广告图片URL")
    private String imageUrl;

    @Schema(description = "广告链接URL")
    private String linkUrl;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static AdResponse from(Ad entity) {
        return AdResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .imageUrl(entity.getImageUrl())
                .linkUrl(entity.getLinkUrl())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
