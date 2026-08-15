package com.coinmarket.order.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.common.notification.NotificationService;
import com.coinmarket.order.dto.OrderCreateRequest;
import com.coinmarket.order.dto.OrderResponse;
import com.coinmarket.order.entity.Order;
import com.coinmarket.order.entity.OrderItem;
import com.coinmarket.order.repository.OrderLogRepository;
import com.coinmarket.order.repository.OrderRepository;
import com.coinmarket.payment.service.PaymentService;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

/**
 * Service-level integration test verifying {@link OrderService} works end-to-end
 * against the real JPA layer backed by H2 (test profile). External collaborators
 * (RabbitMQ, payment gateway, notifications) are mocked so no real infrastructure
 * is contacted.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderService Integration Test")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderLogRepository orderLogRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private NotificationService notificationService;

    private Product product;

    @BeforeEach
    void seedProduct() {
        product = productRepository.save(Product.builder()
                .sellerId(2L)
                .title("1895 Morgan Silver Dollar")
                .price(new BigDecimal("1299.99"))
                .currency("USD")
                .stock(5)
                .status("ACTIVE")
                .viewCount(0)
                .salesCount(0)
                .build());
    }

    private OrderCreateRequest orderRequest(int quantity) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(product.getId());
        request.setQuantity(quantity);
        request.setShippingAddress("123 Main St");
        request.setPaymentMethod("stripe");
        return request;
    }

    @Test
    @DisplayName("创建订单持久化到数据库并扣减库存")
    void createOrder_persistsOrderAndReducesStock() {
        OrderResponse response = orderService.createOrder(10L, orderRequest(1));

        assertThat(response.getStatus()).isEqualTo("PENDING_PAYMENT");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1299.99"));
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductTitle()).isEqualTo("1895 Morgan Silver Dollar");

        // Order + item + log persisted to the database
        Order stored = orderRepository.findByOrderNo(response.getOrderNo()).orElseThrow();
        assertThat(stored.getBuyerId()).isEqualTo(10L);
        assertThat(stored.getSellerId()).isEqualTo(2L);
        assertThat(stored.getItems()).hasSize(1);
        assertThat(orderLogRepository.findByOrderIdOrderByCreatedAtAsc(stored.getId())).hasSize(1);

        // Stock was reduced
        assertThat(product.getStock()).isEqualTo(4);

        // External collaborators invoked (mocked, so no real infra touched)
        verify(rabbitTemplate).convertAndSend(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(stored.getId()));
        verify(notificationService).notifyNewOrder(stored.getId(), stored.getOrderNo());
    }

    @Test
    @DisplayName("库存不足时创建订单抛出异常且不持久化")
    void createOrder_insufficientStock_throwsAndDoesNotPersist() {
        assertThatThrownBy(() -> orderService.createOrder(10L, orderRequest(10)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("库存不足");

        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(orderLogRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("按卖家查询已持久化订单")
    void getSellerOrders_returnsPersistedOrders() {
        seedOrder(2L, 10L, "PAID", "ORD-SELLER-IT-001");

        List<OrderResponse> orders = orderService.getSellerOrders(2L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderNo()).isEqualTo("ORD-SELLER-IT-001");
        assertThat(orders.get(0).getSellerId()).isEqualTo(2L);
        assertThat(orders.get(0).getStatus()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("按买家查询已持久化订单")
    void getBuyerOrders_returnsPersistedOrders() {
        seedOrder(2L, 10L, "COMPLETED", "ORD-BUYER-IT-001");

        List<OrderResponse> orders = orderService.getBuyerOrders(10L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getBuyerId()).isEqualTo(10L);
        assertThat(orders.get(0).getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("支付成功后订单状态流转并持久化")
    void markAsPaid_updatesPersistedStatus() {
        OrderResponse created = orderService.createOrder(10L, orderRequest(1));

        // Detach the entities created above so the next service call re-loads them
        // from the database (mirrors production's transaction-per-request boundary)
        entityManager.clear();

        orderService.markAsPaid(created.getId(), 10L);

        Order stored = orderRepository.findById(created.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo("PAID");
        assertThat(stored.getPaidAt()).isNotNull();

        // Second status change is recorded as an additional order log
        assertThat(orderLogRepository.findByOrderIdOrderByCreatedAtAsc(stored.getId())).hasSize(2);
    }

    private void seedOrder(Long sellerId, Long buyerId, String status, String orderNo) {
        OrderItem item = OrderItem.builder()
                .productId(999L)
                .productTitle("Seeded Coin")
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("50.00"))
                .build();

        Order order = Order.builder()
                .orderNo(orderNo)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .status(status)
                .totalAmount(new BigDecimal("50.00"))
                .currency("USD")
                .shippingAddress("123 Test St")
                .items(List.of(item))
                .build();
        item.setOrder(order);
        orderRepository.save(order);
    }
}
