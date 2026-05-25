package com.coinmarket.order.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.order.dto.BuyerOrderItemResponse;
import com.coinmarket.order.dto.OrderCreateBatchRequest;
import com.coinmarket.order.dto.OrderCreateRequest;
import com.coinmarket.order.dto.OrderItemResponse;
import com.coinmarket.order.dto.OrderLogResponse;
import com.coinmarket.order.dto.OrderResponse;
import com.coinmarket.order.entity.Order;
import com.coinmarket.order.entity.OrderItem;
import com.coinmarket.order.entity.OrderLog;
import com.coinmarket.order.repository.OrderLogRepository;
import com.coinmarket.order.repository.OrderRepository;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import com.coinmarket.user.entity.User;
import com.coinmarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.coinmarket.common.config.RabbitMqConfig.EXCHANGE_ORDER;
import static com.coinmarket.common.config.RabbitMqConfig.ROUTING_KEY_PAYMENT;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLogRepository orderLogRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public OrderResponse createOrder(Long buyerId, OrderCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        BigDecimal totalAmount = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .productTitle(product.getTitle())
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .subtotal(totalAmount)
                .build();

        Order order = Order.builder()
                .orderNo(generateOrderNo())
                .buyerId(buyerId)
                .sellerId(product.getSellerId())
                .status("PENDING_PAYMENT")
                .totalAmount(totalAmount)
                .currency(product.getCurrency())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .shippingMethod(request.getShippingMethod())
                .buyerNote(request.getBuyerNote())
                .items(List.of(item))
                .build();

        // Set order reference on items before saving (JPA cascade needs it)
        order.getItems().forEach(i -> i.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        // Reduce stock
        product.setStock(product.getStock() - request.getQuantity());
        productRepository.save(product);

        // Log
        saveOrderLog(savedOrder.getId(), null, "PENDING_PAYMENT", "系统", "订单创建");

        // Send payment event
        rabbitTemplate.convertAndSend(EXCHANGE_ORDER, ROUTING_KEY_PAYMENT, savedOrder.getId());

        return toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse createBatchOrder(Long buyerId, OrderCreateBatchRequest request) {
        if (request.getItems().isEmpty()) {
            throw new BusinessException("订单必须包含至少一个商品");
        }

        List<OrderItem> orderItems = new java.util.ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        String currency = "USD";
        Long sellerId = null;

        for (var itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new BusinessException("商品不存在: " + itemReq.getProductId()));

            if (product.getStock() < itemReq.getQuantity()) {
                throw new BusinessException("库存不足: " + product.getTitle());
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            currency = product.getCurrency();

            if (sellerId == null) sellerId = product.getSellerId();

            orderItems.add(OrderItem.builder()
                    .productId(product.getId())
                    .productTitle(product.getTitle())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build());

            // Reduce stock
            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepository.save(product);
        }

        Order order = Order.builder()
                .orderNo(generateOrderNo())
                .buyerId(buyerId)
                .sellerId(sellerId)
                .status("PENDING_PAYMENT")
                .totalAmount(totalAmount)
                .currency(currency)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .shippingMethod(request.getShippingMethod())
                .buyerNote(request.getBuyerNote())
                .items(orderItems)
                .build();

        order.getItems().forEach(i -> i.setOrder(order));

        Order savedOrder = orderRepository.save(order);
        saveOrderLog(savedOrder.getId(), null, "PENDING_PAYMENT", "系统", "订单创建");

        // Send payment event
        rabbitTemplate.convertAndSend(EXCHANGE_ORDER, ROUTING_KEY_PAYMENT, savedOrder.getId());

        return toResponse(savedOrder);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        String prev = order.getStatus();
        if (!"PENDING_PAYMENT".equals(prev) && !"PAID".equals(prev)) {
            throw new BusinessException("当前状态不允许取消");
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
        saveOrderLog(orderId, prev, "CANCELLED", "买家", "买家取消订单");
    }

    @Transactional
    public void markAsPaid(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("当前状态不允许支付");
        }
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        saveOrderLog(orderId, "PENDING_PAYMENT", "PAID", "系统", "支付成功");
    }

    @Transactional
    public void markAsShipped(Long orderId, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException("当前状态不允许发货");
        }
        String prev = order.getStatus();
        order.setStatus("SHIPPED");
        orderRepository.save(order);
        saveOrderLog(orderId, prev, "SHIPPED", "卖家", "卖家已发货");
    }

    @Transactional
    public void markAsDelivered(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new BusinessException("当前状态不允许确认收货");
        }
        String prev = order.getStatus();
        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);
        saveOrderLog(orderId, prev, "COMPLETED", "买家", "买家确认收货");
    }

    @Transactional
    public void forceCompleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);
        saveOrderLog(orderId, order.getStatus(), "COMPLETED", "管理员", "管理员强制完成");
    }

    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        return toResponse(order);
    }

    public List<OrderResponse> getBuyerOrders(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getSellerOrders(Long sellerId) {
        return orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getBuyerUnshippedOrders(Long buyerId) {
        return orderRepository.findByBuyerIdAndStatusInOrderByCreatedAtDesc(
                buyerId, List.of("PAID", "SHIPPED"))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BuyerOrderItemResponse> getBuyerDistinctProducts(Long buyerId) {
        List<Object[]> results = orderRepository.findDistinctProductsByBuyerId(buyerId);
        return results.stream().map(row -> {
            Long productId = (Long) row[0];
            String productTitle = (String) row[1];
            java.math.BigDecimal unitPrice = (java.math.BigDecimal) row[2];
            Long orderId = (Long) row[3];

            String productImage = productRepository.findById(productId)
                    .map(p -> p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl())
                    .orElse(null);

            return BuyerOrderItemResponse.builder()
                    .productId(productId)
                    .productTitle(productTitle)
                    .productImage(productImage)
                    .unitPrice(unitPrice)
                    .orderId(orderId)
                    .build();
        }).collect(Collectors.toList());
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void saveOrderLog(Long orderId, String from, String to, String operator, String note) {
        OrderLog log = OrderLog.builder()
                .orderId(orderId)
                .fromStatus(from)
                .toStatus(to)
                .operator(operator)
                .note(note)
                .build();
        orderLogRepository.save(log);
    }

    private OrderResponse toResponse(Order order) {
        String buyerName = "";
        if (order.getBuyerId() != null) {
            buyerName = userRepository.findById(order.getBuyerId())
                    .map(User::getUsername)
                    .orElse("");
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .buyerId(order.getBuyerId())
                .buyerName(buyerName)
                .sellerId(order.getSellerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .shippingMethod(order.getShippingMethod())
                .buyerNote(order.getBuyerNote())
                .paidAt(order.getPaidAt())
                .completedAt(order.getCompletedAt())
                .items(order.getItems().stream()
                        .map(i -> OrderItemResponse.builder()
                                .id(i.getId())
                                .productId(i.getProductId())
                                .productTitle(i.getProductTitle())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .subtotal(i.getSubtotal())
                                .build())
                        .collect(Collectors.toList()))
                .logs(orderLogRepository.findByOrderIdOrderByCreatedAtAsc(order.getId())
                        .stream()
                        .map(l -> OrderLogResponse.builder()
                                .fromStatus(l.getFromStatus())
                                .toStatus(l.getToStatus())
                                .operator(l.getOperator())
                                .note(l.getNote())
                                .createdAt(l.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .build();
    }
}
