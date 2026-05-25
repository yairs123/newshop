package com.coinmarket.admin.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryEntryResponse {
    private Long id;
    private Long productId;
    private String barcode;
    private String title;
    private String country;
    private String category;
    private String denomination;
    private String grade;
    private Integer quantity;
    private BigDecimal purchasePrice;
    private String currency;
    private String supplier;
    private String invoiceNo;
    private LocalDate batchDate;
    private String receiptImage;
    private LocalDateTime createdAt;
}
