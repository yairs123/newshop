package com.coinmarket.common.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyOrderStatusChange(Long orderId, String status) {
        messagingTemplate.convertAndSend("/topic/orders/" + orderId,
                Map.of("type", "ORDER_STATUS", "orderId", orderId, "status", status));
    }

    public void notifyLowStock(Long productId, String title, int stock) {
        messagingTemplate.convertAndSend("/topic/admin/notifications",
                Map.of("type", "LOW_STOCK", "productId", productId, "title", title, "stock", stock));
    }

    public void notifyNewOrder(Long orderId, String orderNo) {
        messagingTemplate.convertAndSend("/topic/admin/notifications",
                Map.of("type", "NEW_ORDER", "orderId", orderId, "orderNo", orderNo));
    }
}
