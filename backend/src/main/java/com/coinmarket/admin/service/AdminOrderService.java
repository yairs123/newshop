package com.coinmarket.admin.service;

import com.coinmarket.order.dto.OrderResponse;
import com.coinmarket.order.entity.Order;
import com.coinmarket.order.repository.OrderRepository;
import com.coinmarket.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

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
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderResponse getOrder(Long id) {
        return orderService.getOrder(id);
    }

    @Transactional
    public void forceComplete(Long orderId) {
        orderService.forceCompleteOrder(orderId);
    }
}
