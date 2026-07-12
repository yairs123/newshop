package com.coinmarket.admin.dto;

import com.coinmarket.admin.entity.BarcodeCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "条码编码响应信息")
public class BarcodeCodeResponse {
    @Schema(description = "编码ID")
    private Long id;

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

    public static BarcodeCodeResponse from(BarcodeCode entity) {
        return BarcodeCodeResponse.builder()
                .id(entity.getId())
                .codeType(entity.getCodeType())
                .codeValue(entity.getCodeValue())
                .labelEn(entity.getLabelEn())
                .labelZh(entity.getLabelZh())
                .parentType(entity.getParentType())
                .parentValue(entity.getParentValue())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .build();
    }
}
