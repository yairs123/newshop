package com.coinmarket.order.controller;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.common.security.JwtAuthenticationFilter;
import com.coinmarket.common.security.JwtTokenProvider;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.order.dto.BuyerOrderItemResponse;
import com.coinmarket.order.dto.OrderCreateBatchRequest;
import com.coinmarket.order.dto.OrderCreateRequest;
import com.coinmarket.order.dto.OrderResponse;
import com.coinmarket.order.service.OrderService;
import com.coinmarket.payment.dto.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        testPrincipal = new UserPrincipal(1L, "testbuyer",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        var auth = new UsernamePasswordAuthenticationToken(testPrincipal, null, testPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrderShouldReturn201() throws Exception {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productId(10L)
                .quantity(2)
                .shippingAddress("123 Main St")
                .paymentMethod("credit_card")
                .buyerNote("Please handle with care")
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(100L)
                .orderNo("ORD-20260701-001")
                .buyerId(1L)
                .buyerName("testbuyer")
                .status("PENDING")
                .totalAmount(new BigDecimal("50.00"))
                .currency("USD")
                .build();

        given(orderService.createOrder(eq(1L), any(OrderCreateRequest.class))).willReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.orderNo").value("ORD-20260701-001"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.buyerName").value("testbuyer"));
    }

    @Test
    void createOrderShouldReturn400WhenValidationFails() throws Exception {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productId(null)
                .quantity(0)
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getOrderShouldReturn200() throws Exception {
        OrderResponse orderResponse = OrderResponse.builder()
                .id(100L)
                .orderNo("ORD-20260701-001")
                .buyerId(1L)
                .buyerName("testbuyer")
                .sellerId(5L)
                .status("PENDING")
                .totalAmount(new BigDecimal("50.00"))
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .build();

        given(orderService.getOrder(100L)).willReturn(orderResponse);

        mockMvc.perform(get("/api/orders/100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.orderNo").value("ORD-20260701-001"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.buyerName").value("testbuyer"));
    }

    @Test
    void getOrderShouldReturn404WhenNotFound() throws Exception {
        given(orderService.getOrder(999L))
                .willThrow(new BusinessException(404, "Order not found: 999"));

        mockMvc.perform(get("/api/orders/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Order not found: 999"));
    }

    @Test
    void cancelOrderShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/orders/100/cancel")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(orderService).cancelOrder(100L, 1L);
    }

    @Test
    void payOrderShouldReturn200() throws Exception {
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .transactionNo("TXN123456")
                .paymentUrl("https://pay.example.com/txn123456")
                .status("PENDING")
                .build();

        given(orderService.processPayment(eq(100L), eq(1L), isNull(), isNull()))
                .willReturn(paymentResponse);

        mockMvc.perform(post("/api/orders/100/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.transactionNo").value("TXN123456"))
                .andExpect(jsonPath("$.data.paymentUrl").value("https://pay.example.com/txn123456"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getBuyerOrdersShouldReturn200() throws Exception {
        OrderResponse order1 = OrderResponse.builder()
                .id(100L)
                .orderNo("ORD-001")
                .buyerId(1L)
                .status("PENDING")
                .totalAmount(new BigDecimal("50.00"))
                .currency("USD")
                .build();
        OrderResponse order2 = OrderResponse.builder()
                .id(101L)
                .orderNo("ORD-002")
                .buyerId(1L)
                .status("SHIPPED")
                .totalAmount(new BigDecimal("75.00"))
                .currency("USD")
                .build();

        given(orderService.getBuyerOrders(1L)).willReturn(List.of(order1, order2));

        mockMvc.perform(get("/api/orders/buyer")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].orderNo").value("ORD-001"))
                .andExpect(jsonPath("$.data[1].orderNo").value("ORD-002"));
    }

    @Test
    void getBuyerItemsShouldReturn200() throws Exception {
        BuyerOrderItemResponse item = BuyerOrderItemResponse.builder()
                .productId(10L)
                .productTitle("Ancient Coin")
                .productImage("coin.jpg")
                .unitPrice(new BigDecimal("25.00"))
                .lastPurchasedAt(LocalDateTime.now())
                .orderId(100L)
                .build();

        given(orderService.getBuyerDistinctProducts(1L)).willReturn(List.of(item));

        mockMvc.perform(get("/api/orders/buyer/items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].productId").value(10))
                .andExpect(jsonPath("$.data[0].productTitle").value("Ancient Coin"))
                .andExpect(jsonPath("$.data[0].orderId").value(100));
    }

    @Test
    void getBuyerUnshippedShouldReturn200() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(100L)
                .orderNo("ORD-001")
                .buyerId(1L)
                .status("PAID")
                .totalAmount(new BigDecimal("50.00"))
                .currency("USD")
                .build();

        given(orderService.getBuyerUnshippedOrders(1L)).willReturn(List.of(order));

        mockMvc.perform(get("/api/orders/buyer/unshipped")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[0].status").value("PAID"));
    }

    @Test
    void getSellerOrdersShouldReturn200() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(100L)
                .orderNo("ORD-001")
                .sellerId(5L)
                .status("PENDING")
                .totalAmount(new BigDecimal("50.00"))
                .currency("USD")
                .build();

        given(orderService.getSellerOrders(1L)).willReturn(List.of(order));

        mockMvc.perform(get("/api/orders/seller")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[0].sellerId").value(5));
    }

    @Test
    void createBatchOrderShouldReturn201() throws Exception {
        OrderCreateBatchRequest item1 = new OrderCreateBatchRequest();
        item1.setItems(List.of(new OrderCreateBatchRequest.OrderItemRequest(10L, 1)));
        item1.setShippingAddress("123 Main St");
        item1.setPaymentMethod("credit_card");

        OrderResponse orderResponse = OrderResponse.builder()
                .id(100L)
                .orderNo("ORD-BATCH-001")
                .buyerId(1L)
                .status("PENDING")
                .totalAmount(new BigDecimal("50.00"))
                .currency("USD")
                .build();

        given(orderService.createBatchOrder(eq(1L), any(OrderCreateBatchRequest.class))).willReturn(orderResponse);

        mockMvc.perform(post("/api/orders/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.orderNo").value("ORD-BATCH-001"));
    }

    @Test
    void shipOrderShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/orders/100/ship")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingCompany\":\"UPS\",\"trackingNumber\":\"1Z999AA10123456784\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(orderService).markAsShipped(eq(100L), eq(1L), eq("UPS"), eq("1Z999AA10123456784"));
    }

    @Test
    void completeOrderShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/orders/100/complete")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(orderService).markAsDelivered(100L, 1L);
    }
}
