package com.coinmarket.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    @NotNull
    private Long productId;

    @Min(1)
    private int quantity;

    private String shippingAddress;
    private String paymentMethod;
    private String shippingMethod;
    private String buyerNote;
}
