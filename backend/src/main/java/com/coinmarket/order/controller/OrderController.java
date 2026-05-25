package com.coinmarket.order.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.order.dto.BuyerOrderItemResponse;
import com.coinmarket.order.dto.OrderCreateBatchRequest;
import com.coinmarket.order.dto.OrderCreateRequest;
import com.coinmarket.order.dto.OrderResponse;
import com.coinmarket.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> createOrder(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(orderService.createOrder(principal.getId(), request));
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> createBatchOrder(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody OrderCreateBatchRequest request) {
        return ApiResponse.success(orderService.createBatchOrder(principal.getId(), request));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<OrderResponse> payOrder(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        orderService.markAsPaid(id, principal.getId());
        return ApiResponse.success(orderService.getOrder(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrder(id));
    }

    @GetMapping("/buyer")
    public ApiResponse<List<OrderResponse>> getBuyerOrders(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(orderService.getBuyerOrders(principal.getId()));
    }

    @GetMapping("/buyer/items")
    public ApiResponse<List<BuyerOrderItemResponse>> getBuyerItems(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(orderService.getBuyerDistinctProducts(principal.getId()));
    }

    @GetMapping("/buyer/unshipped")
    public ApiResponse<List<OrderResponse>> getBuyerUnshipped(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(orderService.getBuyerUnshippedOrders(principal.getId()));
    }

    @GetMapping("/seller")
    public ApiResponse<List<OrderResponse>> getSellerOrders(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(orderService.getSellerOrders(principal.getId()));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelOrder(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        orderService.cancelOrder(id, principal.getId());
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/ship")
    public ApiResponse<Void> shipOrder(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        orderService.markAsShipped(id, principal.getId());
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Void> completeOrder(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        orderService.markAsDelivered(id, principal.getId());
        return ApiResponse.success(null);
    }
}
