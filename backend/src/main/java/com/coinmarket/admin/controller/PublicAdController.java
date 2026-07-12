package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.AdResponse;
import com.coinmarket.admin.service.AdService;
import com.coinmarket.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Tag(name = "公开广告接口", description = "对外公开的广告接口，无需认证即可访问")
public class PublicAdController {

    private final AdService adService;

    @GetMapping("/active")
    @Operation(summary = "获取活跃广告", description = "获取当前活跃的广告列表（公开接口，无需认证）")
    public ApiResponse<List<AdResponse>> getActiveAds() {
        return ApiResponse.success(adService.getActiveAds());
    }
}
