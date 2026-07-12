package com.coinmarket.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存出库请求参数")
public class InventoryOutboundRequest {
    @Schema(description = "出库数量")
    @NotNull
    @Min(1)
    private Integer quantity;

    @Schema(description = "出库原因或备注")
    private String reason;
}
