package com.coinmarket.seller.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.seller.dto.SellerApplicationRequest;
import com.coinmarket.seller.dto.SellerDashboardResponse;
import com.coinmarket.seller.dto.SellerStatusResponse;
import com.coinmarket.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@Tag(name = "卖家管理", description = "卖家申请和管理接口")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/apply")
    @Operation(summary = "申请成为卖家", description = "提交卖家入驻申请")
    public ApiResponse<Void> apply(@Valid @RequestBody SellerApplicationRequest request,
                                   @CurrentUser UserPrincipal principal) {
        sellerService.submitApplication(principal.getId(), request);
        return ApiResponse.success(null);
    }

    @GetMapping("/status")
    @Operation(summary = "获取卖家状态", description = "获取当前用户的卖家申请状态")
    public ApiResponse<SellerStatusResponse> getStatus(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(sellerService.getStatus(principal.getId()));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取卖家面板", description = "获取卖家后台统计数据")
    public ApiResponse<SellerDashboardResponse> getDashboard(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(sellerService.getDashboardStats(principal.getId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新卖家资料", description = "修改店铺名称和描述")
    public ApiResponse<Void> updateProfile(
            @RequestBody Map<String, String> body,
            @CurrentUser UserPrincipal principal) {
        sellerService.updateProfile(principal.getId(), body.get("shopName"), body.get("shopDescription"));
        return ApiResponse.success(null);
    }
}
