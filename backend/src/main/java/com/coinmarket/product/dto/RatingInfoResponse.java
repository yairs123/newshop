package com.coinmarket.product.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingInfoResponse {
    private String grade;
    private String certNumber;
    private boolean verified;
}
