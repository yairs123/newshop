package com.coinmarket.admin.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AdRequest {
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDate startDate;
    private LocalDate endDate;
}
