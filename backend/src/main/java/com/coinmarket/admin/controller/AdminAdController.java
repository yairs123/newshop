package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.AdRequest;
import com.coinmarket.admin.dto.AdResponse;
import com.coinmarket.admin.service.AdService;
import com.coinmarket.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAdController {

    private final AdService adService;

    @GetMapping
    public ApiResponse<List<AdResponse>> listAll() {
        return ApiResponse.success(adService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(adService.getById(id));
    }

    @PostMapping
    public ApiResponse<AdResponse> create(@RequestBody AdRequest request) {
        return ApiResponse.success(adService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdResponse> update(@PathVariable Long id, @RequestBody AdRequest request) {
        return ApiResponse.success(adService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/toggle-status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id) {
        adService.toggleStatus(id);
        return ApiResponse.success(null);
    }
}
