package com.coinmarket.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "卖家申请请求参数")
public class SellerApplicationRequest {
    @NotBlank(message = "Shop name is required")
    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺描述")
    private String shopDescription;

    @NotBlank(message = "ID document is required")
    @Schema(description = "身份证明文件URL")
    private String idDocumentUrl;

    @Schema(description = "证件类型")
    private String idDocumentType;
}
