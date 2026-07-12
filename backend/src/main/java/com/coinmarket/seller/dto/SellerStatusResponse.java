package com.coinmarket.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "卖家状态响应")
public class SellerStatusResponse {
    @Schema(description = "是否已申请")
    private boolean hasApplied;

    @Schema(description = "申请状态")
    private String status;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "是否已完善资料")
    private boolean hasProfile;

    @Schema(description = "是否已锁定")
    private boolean locked;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺描述")
    private String shopDescription;
}
