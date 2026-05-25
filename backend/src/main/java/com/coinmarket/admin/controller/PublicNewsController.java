package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.NewsResponse;
import com.coinmarket.admin.service.NewsService;
import com.coinmarket.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class PublicNewsController {

    private final NewsService newsService;

    @GetMapping
    public ApiResponse<List<NewsResponse>> getPublishedNews() {
        return ApiResponse.success(newsService.getPublishedNews());
    }

    @GetMapping("/{id}")
    public ApiResponse<NewsResponse> getNewsById(@PathVariable Long id) {
        return ApiResponse.success(newsService.getPublishedNewsById(id));
    }
}
