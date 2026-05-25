package com.coinmarket.admin.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryEntryRequest {
    // Product identification fields (for barcode generation)
    private String countryCode;
    private String categoryCode;
    private String denominationCode;
    private String eraCode;
    private String gradeCode;

    // Product details
    private String title;
    private String description;
    private String material;
    private BigDecimal weight;
    private Integer year;
    private String ratingCompany;
    private String ratingNumber;
    private String ratingGrade;

    // Purchase / batch info
    private Integer quantity;
    private BigDecimal purchasePrice;
    private String currency;
    private String supplier;
    private String invoiceNo;
    private LocalDate batchDate;
    private String receiptImage;
}
