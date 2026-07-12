package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建/编辑费用类别请求")
public class ExpenseCategoryRequest {

    @NotBlank
    @Schema(description = "类别名称")
    private String name;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "排序")
    private Integer sortOrder;
}
