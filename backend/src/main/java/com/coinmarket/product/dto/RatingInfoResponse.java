package com.coinmarket.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评级信息响应")
public class RatingInfoResponse {
    @Schema(description = "评级等级")
    private String grade;

    @Schema(description = "证书编号")
    private String certNumber;

    @Schema(description = "是否已验证")
    private boolean verified;
}
