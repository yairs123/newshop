package com.coinmarket.seller.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerStatusResponse {
    private boolean hasApplied;
    private String status;
    private String rejectReason;
    private boolean hasProfile;
    private boolean locked;
}
