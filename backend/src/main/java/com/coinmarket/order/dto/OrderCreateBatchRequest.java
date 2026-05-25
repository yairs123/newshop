package com.coinmarket.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateBatchRequest {

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

    private String shippingAddress;
    private String paymentMethod;
    private String shippingMethod;
    private String buyerNote;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private int quantity;
    }
}
