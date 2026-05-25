package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminOrderService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        var result = adminOrderService.listOrders(pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long id) {
        return ApiResponse.success(adminOrderService.getOrder(id));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Void> forceComplete(@PathVariable Long id) {
        adminOrderService.forceComplete(id);
        return ApiResponse.success(null);
    }
}
