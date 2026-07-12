package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminOrderService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.order.dto.AdminOrderUpdateRequest;
import com.coinmarket.order.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "后台订单管理", description = "管理员订单管理接口，包括订单查询、发货、取消、强制完成等操作")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    @Operation(summary = "获取订单列表", description = "分页查询所有订单")
    public ApiResponse<PageResponse<OrderResponse>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        var result = adminOrderService.listOrders(pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long id) {
        return ApiResponse.success(adminOrderService.getOrder(id));
    }

    @PostMapping("/{id}/mark-paid")
    @Operation(summary = "标记已付款", description = "将指定订单标记为已付款状态")
    public ApiResponse<Void> markPaid(@PathVariable Long id) {
        adminOrderService.markPaid(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消指定订单")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id) {
        adminOrderService.cancelOrder(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "强制完成", description = "强制将订单标记为已完成状态")
    public ApiResponse<Void> forceComplete(@PathVariable Long id) {
        adminOrderService.forceComplete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/ship")
    @Operation(summary = "发货", description = "为订单填写物流信息（运单号和物流公司）并标记为已发货")
    public ApiResponse<Void> shipOrder(
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            @RequestParam String trackingCompany) {
        adminOrderService.shipOrder(id, trackingNumber, trackingCompany);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新订单", description = "更新订单信息（如地址、备注等）")
    public ApiResponse<Void> updateOrder(
            @PathVariable Long id,
            @RequestBody AdminOrderUpdateRequest request) {
        adminOrderService.updateOrder(id, request);
        return ApiResponse.success(null);
    }
}
