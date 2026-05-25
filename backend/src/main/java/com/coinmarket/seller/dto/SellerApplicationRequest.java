package com.coinmarket.seller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerApplicationRequest {
    @NotBlank(message = "Shop name is required")
    private String shopName;

    private String shopDescription;

    @NotBlank(message = "ID document is required")
    private String idDocumentUrl;

    private String idDocumentType;
}
