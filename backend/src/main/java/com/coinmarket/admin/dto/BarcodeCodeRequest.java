package com.coinmarket.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "条码编码创建/更新请求参数")
public class BarcodeCodeRequest {
    @Schema(description = "编码类型")
    private String codeType;

    @Schema(description = "编码值")
    private String codeValue;

    @Schema(description = "英文标签")
    private String labelEn;

    @Schema(description = "中文标签")
    private String labelZh;

    @Schema(description = "父级类型")
    private String parentType;

    @Schema(description = "父级值")
    private String parentValue;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean isActive;
}
