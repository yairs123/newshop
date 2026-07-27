package com.coinmarket.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品评价汇总信息")
public class ReviewSummary {

    @Schema(description = "平均评分")
    private Double averageRating;

    @Schema(description = "评价总数")
    private Long totalReviews;
}
