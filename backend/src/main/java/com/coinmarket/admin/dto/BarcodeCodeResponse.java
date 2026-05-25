package com.coinmarket.admin.dto;

import com.coinmarket.admin.entity.BarcodeCode;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BarcodeCodeResponse {
    private Long id;
    private String codeType;
    private String codeValue;
    private String labelEn;
    private String labelZh;
    private String parentType;
    private String parentValue;
    private Integer sortOrder;
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
