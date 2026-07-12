package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.NewsResponse;
import com.coinmarket.admin.service.NewsService;
import com.coinmarket.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "公开新闻接口", description = "对外公开的新闻接口，无需认证即可访问")
public class PublicNewsController {

    private final NewsService newsService;

    @GetMapping
    @Operation(summary = "获取已发布新闻", description = "获取所有已发布的新闻列表（公开接口，无需认证）")
    public ApiResponse<List<NewsResponse>> getPublishedNews() {
        return ApiResponse.success(newsService.getPublishedNews());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取新闻详情", description = "根据ID获取已发布的新闻详情（公开接口，无需认证）")
    public ApiResponse<NewsResponse> getNewsById(@PathVariable Long id) {
        return ApiResponse.success(newsService.getPublishedNewsById(id));
    }
}
