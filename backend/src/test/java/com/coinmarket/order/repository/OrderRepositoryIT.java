package com.coinmarket.order.repository;

import com.coinmarket.order.entity.Order;
import com.coinmarket.order.entity.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Order Repository Integration Test")
class OrderRepositoryIT {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("保存订单并包含订单项")
    void saveOrderWithItems() {
        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productTitle("Test Coin")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();

        Order order = Order.builder()
                .orderNo("ORD-TEST-001")
                .buyerId(10L)
                .sellerId(20L)
                .status("PENDING_PAYMENT")
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .shippingAddress("123 Test St")
                .items(List.of(item))
                .build();

        item.setOrder(order);
        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderNo()).isEqualTo("ORD-TEST-001");
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getProductTitle()).isEqualTo("Test Coin");
    }

    @Test
    @DisplayName("按卖家ID查询订单")
    void findBySellerId() {
        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productTitle("Seller Coin")
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("50.00"))
                .build();

        Order order = Order.builder()
                .orderNo("ORD-SELLER-001")
                .buyerId(10L)
                .sellerId(99L)
                .status("PAID")
                .totalAmount(new BigDecimal("50.00"))
                .currency("USD")
                .items(List.of(item))
                .build();
        item.setOrder(order);
        orderRepository.save(order);

        List<Order> orders = orderRepository.findBySellerIdOrderByCreatedAtDesc(99L);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getSellerId()).isEqualTo(99L);
        assertThat(orders.get(0).getStatus()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("按买家ID查询订单")
    void findByBuyerId() {
        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productTitle("Buyer Coin")
                .quantity(2)
                .unitPrice(new BigDecimal("25.00"))
                .subtotal(new BigDecimal("50.00"))
                .build();

        Order order = Order.builder()
                .orderNo("ORD-BUYER-001")
                .buyerId(55L)
                .sellerId(20L)
                .status("COMPLETED")
                .totalAmount(new BigDecimal("50.00"))
                .currency("USD")
                .items(List.of(item))
                .build();
        item.setOrder(order);
        orderRepository.save(order);

        List<Order> orders = orderRepository.findByBuyerIdOrderByCreatedAtDesc(55L);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getBuyerId()).isEqualTo(55L);
    }

    @Test
    @DisplayName("按订单号查询")
    void findByOrderNo() {
        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productTitle("Order No Coin")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("10.00"))
                .build();

        Order order = Order.builder()
                .orderNo("ORD-UNIQUE-999")
                .buyerId(10L)
                .sellerId(20L)
                .status("PENDING_PAYMENT")
                .totalAmount(new BigDecimal("10.00"))
                .currency("USD")
                .items(List.of(item))
                .build();
        item.setOrder(order);
        orderRepository.save(order);

        var found = orderRepository.findByOrderNo("ORD-UNIQUE-999");
        assertThat(found).isPresent();
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("统计卖家已完成订单数")
    void countBySellerIdAndStatus() {
        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productTitle("Count Test Coin")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("10.00"))
                .build();

        Order order = Order.builder()
                .orderNo("ORD-COUNT-001")
                .buyerId(10L)
                .sellerId(77L)
                .status("COMPLETED")
                .totalAmount(new BigDecimal("10.00"))
                .currency("USD")
                .items(List.of(item))
                .build();
        item.setOrder(order);
        orderRepository.save(order);

        long count = orderRepository.countBySellerIdAndStatus(77L, "COMPLETED");
        assertThat(count).isEqualTo(1L);

        long noMatch = orderRepository.countBySellerIdAndStatus(77L, "CANCELLED");
        assertThat(noMatch).isZero();
    }
}
