package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.NewsRequest;
import com.coinmarket.admin.dto.NewsResponse;
import com.coinmarket.admin.service.NewsService;
import com.coinmarket.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/news")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "新闻管理", description = "管理员新闻管理接口，包括新闻的增删改查和发布/取消发布")
public class AdminNewsController {

    private final NewsService newsService;

    @GetMapping
    @Operation(summary = "获取新闻列表", description = "查询所有新闻")
    public ApiResponse<List<NewsResponse>> listAll() {
        return ApiResponse.success(newsService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取新闻详情", description = "根据ID获取新闻详细信息")
    public ApiResponse<NewsResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(newsService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建新闻", description = "创建新新闻")
    public ApiResponse<NewsResponse> create(@RequestBody NewsRequest request) {
        return ApiResponse.success(newsService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新新闻", description = "更新新闻信息")
    public ApiResponse<NewsResponse> update(@PathVariable Long id, @RequestBody NewsRequest request) {
        return ApiResponse.success(newsService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除新闻", description = "删除指定新闻")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        newsService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "发布新闻", description = "发布新闻使其对用户可见")
    public ApiResponse<NewsResponse> publish(@PathVariable Long id) {
        return ApiResponse.success(newsService.publish(id));
    }

    @PutMapping("/{id}/unpublish")
    @Operation(summary = "取消发布", description = "取消发布新闻使其对用户不可见")
    public ApiResponse<NewsResponse> unpublish(@PathVariable Long id) {
        return ApiResponse.success(newsService.unpublish(id));
    }
}
