package com.coinmarket.finance.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReimbursementRequest {
    private String title;
    private BigDecimal amount;
    private String currency;
    private String category;
    private String description;
    private String receiptUrl;
}
