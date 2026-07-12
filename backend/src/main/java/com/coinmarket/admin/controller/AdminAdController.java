package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.AdRequest;
import com.coinmarket.admin.dto.AdResponse;
import com.coinmarket.admin.service.AdService;
import com.coinmarket.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "广告管理", description = "管理员广告管理接口，包括广告的增删改查和状态切换")
public class AdminAdController {

    private final AdService adService;

    @GetMapping
    @Operation(summary = "获取广告列表", description = "查询所有广告")
    public ApiResponse<List<AdResponse>> listAll() {
        return ApiResponse.success(adService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取广告详情", description = "根据ID获取广告详细信息")
    public ApiResponse<AdResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(adService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建广告", description = "创建新广告")
    public ApiResponse<AdResponse> create(@RequestBody AdRequest request) {
        return ApiResponse.success(adService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新广告", description = "更新广告信息")
    public ApiResponse<AdResponse> update(@PathVariable Long id, @RequestBody AdRequest request) {
        return ApiResponse.success(adService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除广告", description = "删除指定广告")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "切换广告状态", description = "启用或禁用广告")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id) {
        adService.toggleStatus(id);
        return ApiResponse.success(null);
    }
}
