package com.coinmarket.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "广告创建/更新请求参数")
public class AdRequest {
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
}
