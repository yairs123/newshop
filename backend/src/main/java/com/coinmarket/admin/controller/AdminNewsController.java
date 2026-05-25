package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.NewsRequest;
import com.coinmarket.admin.dto.NewsResponse;
import com.coinmarket.admin.service.NewsService;
import com.coinmarket.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/news")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNewsController {

    private final NewsService newsService;

    @GetMapping
    public ApiResponse<List<NewsResponse>> listAll() {
        return ApiResponse.success(newsService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<NewsResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(newsService.getById(id));
    }

    @PostMapping
    public ApiResponse<NewsResponse> create(@RequestBody NewsRequest request) {
        return ApiResponse.success(newsService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<NewsResponse> update(@PathVariable Long id, @RequestBody NewsRequest request) {
        return ApiResponse.success(newsService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        newsService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/publish")
    public ApiResponse<NewsResponse> publish(@PathVariable Long id) {
        return ApiResponse.success(newsService.publish(id));
    }

    @PutMapping("/{id}/unpublish")
    public ApiResponse<NewsResponse> unpublish(@PathVariable Long id) {
        return ApiResponse.success(newsService.unpublish(id));
    }
}
