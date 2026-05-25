package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.AdResponse;
import com.coinmarket.admin.service.AdService;
import com.coinmarket.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class PublicAdController {

    private final AdService adService;

    @GetMapping("/active")
    public ApiResponse<List<AdResponse>> getActiveAds() {
        return ApiResponse.success(adService.getActiveAds());
    }
}
