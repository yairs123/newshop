package com.coinmarket.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品评价响应信息")
public class ReviewResponse {

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "评分 (1-5)")
    private int rating;

    @Schema(description = "评价内容")
    private String comment;

    @Schema(description = "评价时间")
    private LocalDateTime createdAt;
}
