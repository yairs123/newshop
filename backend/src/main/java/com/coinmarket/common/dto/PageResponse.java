package com.coinmarket.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应封装")
public class PageResponse<T> {
    @Schema(description = "当前页数据列表")
    private List<T> content;

    @Schema(description = "当前页码（从0开始）")
    private int page;

    @Schema(description = "每页大小")
    private int size;

    @Schema(description = "总记录数")
    private long totalElements;

    @Schema(description = "总页数")
    private int totalPages;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
