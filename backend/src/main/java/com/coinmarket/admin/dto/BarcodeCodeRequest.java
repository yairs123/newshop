package com.coinmarket.admin.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BarcodeCodeRequest {
    private String codeType;
    private String codeValue;
    private String labelEn;
    private String labelZh;
    private String parentType;
    private String parentValue;
    private Integer sortOrder;
    private Boolean isActive;
}
