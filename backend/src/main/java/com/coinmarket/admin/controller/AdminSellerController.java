package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminSellerService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.seller.entity.SellerApplication;
import com.coinmarket.seller.entity.SellerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sellers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private final AdminSellerService adminSellerService;

    @GetMapping("/applications")
    public ApiResponse<List<SellerApplication>> listApplications(
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminSellerService.listApplications(status));
    }

    @PostMapping("/applications/{id}/approve")
    public ApiResponse<Void> approveApplication(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        adminSellerService.approveApplication(id, adminId);
        return ApiResponse.success(null);
    }

    @PostMapping("/applications/{id}/reject")
    public ApiResponse<Void> rejectApplication(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestParam String reason) {
        adminSellerService.rejectApplication(id, adminId, reason);
        return ApiResponse.success(null);
    }

    @GetMapping("/profiles")
    public ApiResponse<List<SellerProfile>> listProfiles() {
        return ApiResponse.success(adminSellerService.listProfiles());
    }
}
