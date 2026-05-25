package com.coinmarket.admin.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NewsRequest {
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    private Boolean isPublished;
}
