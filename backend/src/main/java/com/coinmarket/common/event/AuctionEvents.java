package com.coinmarket.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

public final class AuctionEvents {

    @Data
    @AllArgsConstructor
    public static class OrderCompletedEvent {
        private Long orderId;
        private Long buyerId;
        private Long sellerId;
        private BigDecimal amount;
    }

    @Data
    @AllArgsConstructor
    public static class ProductListedEvent {
        private Long productId;
        private Long sellerId;
    }

    private AuctionEvents() {}
}
