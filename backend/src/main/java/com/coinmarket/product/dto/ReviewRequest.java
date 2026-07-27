package com.coinmarket.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品评价请求参数")
public class ReviewRequest {

    @NotNull
    @Schema(description = "商品ID")
    private Long productId;

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "评分 (1-5)")
    private Integer rating;

    @Schema(description = "评价内容")
    private String comment;
}
