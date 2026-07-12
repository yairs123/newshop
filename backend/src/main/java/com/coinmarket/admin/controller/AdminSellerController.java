package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminSellerService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.seller.entity.SellerApplication;
import com.coinmarket.seller.entity.SellerProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sellers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "后台卖家管理", description = "管理员卖家管理接口，包括卖家入驻审核、卖家信息查询等操作")
public class AdminSellerController {

    private final AdminSellerService adminSellerService;

    @GetMapping("/applications")
    @Operation(summary = "获取申请列表", description = "查询卖家入驻申请，可按状态过滤")
    public ApiResponse<List<SellerApplication>> listApplications(
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminSellerService.listApplications(status));
    }

    @PostMapping("/applications/{id}/approve")
    @Operation(summary = "审核通过", description = "通过卖家入驻申请")
    public ApiResponse<Void> approveApplication(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        adminSellerService.approveApplication(id, adminId);
        return ApiResponse.success(null);
    }

    @PostMapping("/applications/{id}/reject")
    @Operation(summary = "审核拒绝", description = "拒绝卖家入驻申请并填写拒绝原因")
    public ApiResponse<Void> rejectApplication(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestParam String reason) {
        adminSellerService.rejectApplication(id, adminId, reason);
        return ApiResponse.success(null);
    }

    @GetMapping("/profiles")
    @Operation(summary = "获取卖家列表", description = "查询所有已入驻卖家信息")
    public ApiResponse<List<SellerProfile>> listProfiles() {
        return ApiResponse.success(adminSellerService.listProfiles());
    }
}
