package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "费用类别响应数据")
public class ExpenseCategoryResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "类别名称")
    private String name;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static ExpenseCategoryResponse from(ExpenseCategory c) {
        return ExpenseCategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .icon(c.getIcon())
                .color(c.getColor())
                .sortOrder(c.getSortOrder())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
