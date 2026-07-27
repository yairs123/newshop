package com.coinmarket.admin.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.common.notification.NotificationService;
import com.coinmarket.order.dto.AdminOrderUpdateRequest;
import com.coinmarket.order.dto.OrderEditLogResponse;
import com.coinmarket.order.dto.OrderResponse;
import com.coinmarket.order.entity.Order;
import com.coinmarket.order.entity.OrderEditLog;
import com.coinmarket.order.repository.OrderEditLogRepository;
import com.coinmarket.order.repository.OrderRepository;
import com.coinmarket.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final OrderEditLogRepository orderEditLogRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toBriefResponse);
    }

    private OrderResponse toBriefResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .trackingNumber(order.getTrackingNumber())
                .trackingCompany(order.getTrackingCompany())
                .adminNote(order.getAdminNote())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderResponse getOrder(Long id) {
        OrderResponse response = orderService.getOrder(id);
        // 补充编辑历史
        List<OrderEditLog> editLogs = orderEditLogRepository.findByOrderIdOrderByCreatedAtAsc(id);
        response.setEditLogs(editLogs.stream()
                .map(OrderEditLogResponse::from)
                .collect(Collectors.toList()));
        return response;
    }

    @Transactional
    public void markPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("当前状态不允许标记已支付");
        }
        String prev = order.getStatus();
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        saveEditLog(orderId, "status", prev, "PAID", "管理员标记已支付");
        notificationService.notifyOrderStatusChange(orderId, "PAID");
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
            throw new BusinessException("当前状态不允许取消");
        }
        String prev = order.getStatus();
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        saveEditLog(orderId, "status", prev, "CANCELLED", "管理员取消订单");
        notificationService.notifyOrderStatusChange(orderId, "CANCELLED");
    }

    @Transactional
    public void forceComplete(Long orderId) {
        orderService.forceCompleteOrder(orderId);
    }

    @Transactional
    public void updateOrder(Long orderId, AdminOrderUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        String reason = request.getReason() != null ? request.getReason() : "管理员手动修改";

        if (request.getPaidAt() != null) {
            String oldVal = order.getPaidAt() != null ? order.getPaidAt().toString() : null;
            order.setPaidAt(request.getPaidAt());
            saveEditLog(orderId, "paidAt", oldVal, request.getPaidAt().toString(), reason);
        }
        if (request.getCompletedAt() != null) {
            String oldVal = order.getCompletedAt() != null ? order.getCompletedAt().toString() : null;
            order.setCompletedAt(request.getCompletedAt());
            saveEditLog(orderId, "completedAt", oldVal, request.getCompletedAt().toString(), reason);
        }
        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
            saveEditLog(orderId, "trackingNumber", null, request.getTrackingNumber(), reason);
        }
        if (request.getTrackingCompany() != null) {
            order.setTrackingCompany(request.getTrackingCompany());
            saveEditLog(orderId, "trackingCompany", null, request.getTrackingCompany(), reason);
        }
        if (request.getAdminNote() != null) {
            String oldVal = order.getAdminNote();
            order.setAdminNote(request.getAdminNote());
            saveEditLog(orderId, "adminNote", oldVal, request.getAdminNote(), reason);
        }

        orderRepository.save(order);
    }

    @Transactional
    public void refundOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        String prev = order.getStatus();
        if (!"PAID".equals(prev) && !"SHIPPED".equals(prev)) {
            throw new BusinessException("当前状态不允许退款");
        }
        order.setStatus("REFUNDED");
        orderRepository.save(order);
        saveEditLog(orderId, "status", prev, "REFUNDED", "管理员退款: " + (reason != null ? reason : ""));
        notificationService.notifyOrderStatusChange(orderId, "REFUNDED");
    }

    @Transactional
    public void shipOrder(Long orderId, String trackingNumber, String trackingCompany) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException("当前状态不允许发货");
        }
        String prevStatus = order.getStatus();
        order.setStatus("SHIPPED");
        order.setTrackingNumber(trackingNumber);
        order.setTrackingCompany(trackingCompany);
        orderRepository.save(order);
        saveEditLog(orderId, "status", prevStatus, "SHIPPED", "管理员发货");
        notificationService.notifyOrderStatusChange(orderId, "SHIPPED");
    }

    private void saveEditLog(Long orderId, String fieldName, String oldValue, String newValue, String reason) {
        OrderEditLog log = OrderEditLog.builder()
                .orderId(orderId)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .reason(reason)
                .operator("管理员")
                .build();
        orderEditLogRepository.save(log);
    }
}
