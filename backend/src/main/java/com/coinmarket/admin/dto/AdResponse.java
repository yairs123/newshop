package com.coinmarket.admin.dto;

import com.coinmarket.admin.entity.Ad;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AdResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    public static AdResponse from(Ad entity) {
        return AdResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .imageUrl(entity.getImageUrl())
                .linkUrl(entity.getLinkUrl())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
