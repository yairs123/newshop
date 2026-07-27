package com.coinmarket.order.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.order.dto.BuyerOrderItemResponse;
import com.coinmarket.order.dto.OrderCreateBatchRequest;
import com.coinmarket.order.dto.OrderCreateRequest;
import com.coinmarket.order.dto.OrderResponse;
import com.coinmarket.order.service.OrderService;
import com.coinmarket.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单的创建、查询、支付、取消、发货、完成等操作")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建订单", description = "买家创建新的订单")
    public ApiResponse<OrderResponse> createOrder(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(orderService.createOrder(principal.getId(), request));
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "批量创建订单", description = "买家批量创建订单，支持多商品组合")
    public ApiResponse<OrderResponse> createBatchOrder(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody OrderCreateBatchRequest request) {
        return ApiResponse.success(orderService.createBatchOrder(principal.getId(), request));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "支付订单", description = "买家对指定订单进行支付")
    public ApiResponse<PaymentResponse> payOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @CurrentUser UserPrincipal principal) {
        String returnUrl = body != null ? body.get("returnUrl") : null;
        String cancelUrl = body != null ? body.get("cancelUrl") : null;
        PaymentResponse result = orderService.processPayment(id, principal.getId(), returnUrl, cancelUrl);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询订单详情", description = "根据订单ID查询订单详细信息")
    public ApiResponse<OrderResponse> getOrder(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(orderService.getOrder(id, principal.getId()));
    }

    @GetMapping("/buyer")
    @Operation(summary = "查询买家订单列表", description = "查询当前买家所有订单")
    public ApiResponse<List<OrderResponse>> getBuyerOrders(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(orderService.getBuyerOrders(principal.getId()));
    }

    @GetMapping("/buyer/items")
    @Operation(summary = "查询买家购买商品", description = "查询买家购买过的商品列表")
    public ApiResponse<List<BuyerOrderItemResponse>> getBuyerItems(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(orderService.getBuyerDistinctProducts(principal.getId()));
    }

    @GetMapping("/buyer/unshipped")
    @Operation(summary = "查询买家未发货订单", description = "查询当前买家未发货的订单列表")
    public ApiResponse<List<OrderResponse>> getBuyerUnshipped(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(orderService.getBuyerUnshippedOrders(principal.getId()));
    }

    @GetMapping("/seller")
    @Operation(summary = "查询卖家订单列表", description = "查询当前卖家的所有订单")
    public ApiResponse<List<OrderResponse>> getSellerOrders(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(orderService.getSellerOrders(principal.getId()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "买家取消指定订单")
    public ApiResponse<Void> cancelOrder(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        orderService.cancelOrder(id, principal.getId());
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/ship")
    @Operation(summary = "发货", description = "卖家标记订单为已发货，可填写快递公司和单号")
    public ApiResponse<Void> shipOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @CurrentUser UserPrincipal principal) {
        String trackingCompany = body != null ? body.get("trackingCompany") : null;
        String trackingNumber = body != null ? body.get("trackingNumber") : null;
        orderService.markAsShipped(id, principal.getId(), trackingCompany, trackingNumber);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成订单", description = "买家确认收货，完成订单")
    public ApiResponse<Void> completeOrder(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        orderService.markAsDelivered(id, principal.getId());
        return ApiResponse.success(null);
    }
}
