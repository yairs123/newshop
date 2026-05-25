package com.coinmarket.seller.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.seller.dto.SellerApplicationRequest;
import com.coinmarket.seller.dto.SellerDashboardResponse;
import com.coinmarket.seller.dto.SellerStatusResponse;
import com.coinmarket.seller.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/apply")
    public ApiResponse<Void> apply(@Valid @RequestBody SellerApplicationRequest request,
                                   @CurrentUser UserPrincipal principal) {
        sellerService.submitApplication(principal.getId(), request);
        return ApiResponse.success(null);
    }

    @GetMapping("/status")
    public ApiResponse<SellerStatusResponse> getStatus(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(sellerService.getStatus(principal.getId()));
    }

    @GetMapping("/dashboard")
    public ApiResponse<SellerDashboardResponse> getDashboard(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(sellerService.getDashboardStats(principal.getId()));
    }
}
