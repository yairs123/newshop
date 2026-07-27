package com.coinmarket.order.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.common.notification.NotificationService;
import com.coinmarket.order.dto.OrderCreateBatchRequest;
import com.coinmarket.order.dto.OrderCreateRequest;
import com.coinmarket.order.entity.Order;
import com.coinmarket.order.entity.OrderItem;
import com.coinmarket.order.repository.OrderLogRepository;
import com.coinmarket.order.repository.OrderRepository;
import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import com.coinmarket.payment.service.PaymentService;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import com.coinmarket.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLogRepository orderLogRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .title("1895 Morgan Silver Dollar")
                .price(new BigDecimal("1299.99"))
                .currency("USD")
                .stock(5)
                .sellerId(2L)
                .build();
        product.setId(1L);

        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productTitle("1895 Morgan Silver Dollar")
                .quantity(1)
                .unitPrice(new BigDecimal("1299.99"))
                .subtotal(new BigDecimal("1299.99"))
                .build();
        item.setId(10L);

        order = Order.builder()
                .orderNo("ORD20260712000001")
                .buyerId(10L)
                .sellerId(2L)
                .status("PENDING_PAYMENT")
                .totalAmount(new BigDecimal("1299.99"))
                .currency("USD")
                .shippingAddress("123 Main St")
                .items(List.of(item))
                .build();
        order.setId(100L);
        item.setOrder(order);
    }

    @Nested
    @DisplayName("创建订单")
    class CreateOrder {

        @Test
        @DisplayName("有效请求创建订单成功")
        void validRequest_createsOrder() {
            OrderCreateRequest request = new OrderCreateRequest();
            request.setProductId(1L);
            request.setQuantity(1);
            request.setShippingAddress("123 Main St");

            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderLogRepository.save(any())).willReturn(null);

            var response = orderService.createOrder(10L, request);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("PENDING_PAYMENT");
            assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1299.99"));
            assertThat(response.getItems()).hasSize(1);
            verify(orderRepository).save(any(Order.class));
            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("商品不存在抛出异常")
        void unknownProduct_throwsBusinessException() {
            OrderCreateRequest request = new OrderCreateRequest();
            request.setProductId(999L);
            request.setQuantity(1);

            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(10L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在");
        }

        @Test
        @DisplayName("库存不足抛出异常")
        void insufficientStock_throwsBusinessException() {
            OrderCreateRequest request = new OrderCreateRequest();
            request.setProductId(1L);
            request.setQuantity(10);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> orderService.createOrder(10L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("库存不足");
        }
    }

    @Nested
    @DisplayName("取消订单")
    class CancelOrder {

        @Test
        @DisplayName("待支付订单可取消")
        void pendingPaymentOrder_cancelsSuccessfully() {
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderLogRepository.save(any())).willReturn(null);

            orderService.cancelOrder(100L, 10L);

            assertThat(order.getStatus()).isEqualTo("CANCELLED");
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("非本人订单抛出异常")
        void otherBuyerOrder_throwsBusinessException() {
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(100L, 99L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("无权操作此订单");
        }

        @Test
        @DisplayName("已完成订单不能取消")
        void completedOrder_throwsBusinessException() {
            order.setStatus("COMPLETED");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(100L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("当前状态不允许取消");
        }
    }

    @Nested
    @DisplayName("订单状态流转")
    class OrderStatusFlow {

        @Test
        @DisplayName("标记为已支付")
        void markAsPaid_success() {
            order.setStatus("PENDING_PAYMENT");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderLogRepository.save(any())).willReturn(null);

            orderService.markAsPaid(100L, 10L);

            assertThat(order.getStatus()).isEqualTo("PAID");
            assertThat(order.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("标记为已发货")
        void markAsShipped_success() {
            order.setStatus("PAID");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderLogRepository.save(any())).willReturn(null);

            orderService.markAsShipped(100L, 2L, "UPS", "1Z999AA10123456784");

            assertThat(order.getStatus()).isEqualTo("SHIPPED");
            assertThat(order.getTrackingCompany()).isEqualTo("UPS");
            assertThat(order.getTrackingNumber()).isEqualTo("1Z999AA10123456784");
        }

        @Test
        @DisplayName("标记为已完成")
        void markAsDelivered_success() {
            order.setStatus("SHIPPED");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderLogRepository.save(any())).willReturn(null);

            orderService.markAsDelivered(100L, 10L);

            assertThat(order.getStatus()).isEqualTo("COMPLETED");
            assertThat(order.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("卖家不能确认收货")
        void sellerCannotMarkDelivered() {
            order.setStatus("SHIPPED");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.markAsDelivered(100L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("无权操作此订单");
        }

        @Test
        @DisplayName("管理员强制完成订单")
        void forceComplete_success() {
            order.setStatus("PAID");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderLogRepository.save(any())).willReturn(null);

            orderService.forceCompleteOrder(100L);

            assertThat(order.getStatus()).isEqualTo("COMPLETED");
        }
    }

    @Nested
    @DisplayName("查询订单")
    class GetOrder {

        @Test
        @DisplayName("按ID查询订单成功")
        void existingOrder_returnsOrder() {
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderLogRepository.findByOrderIdOrderByCreatedAtAsc(100L)).willReturn(List.of());

            var response = orderService.getOrder(100L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getOrderNo()).isEqualTo("ORD20260712000001");
        }

        @Test
        @DisplayName("不存在的订单抛出异常")
        void unknownOrder_throwsBusinessException() {
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrder(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("订单不存在");
        }
    }

    @Nested
    @DisplayName("批量创建订单")
    class CreateBatchOrder {

        @Test
        @DisplayName("有效请求创建批量订单成功")
        void validRequest_createsBatchOrder() {
            OrderCreateBatchRequest request = new OrderCreateBatchRequest();
            OrderCreateBatchRequest.OrderItemRequest itemReq = new OrderCreateBatchRequest.OrderItemRequest();
            itemReq.setProductId(1L);
            itemReq.setQuantity(2);
            request.setItems(java.util.List.of(itemReq));
            request.setShippingAddress("123 Main St");

            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderLogRepository.save(any())).willReturn(null);

            var response = orderService.createBatchOrder(10L, request);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("PENDING_PAYMENT");
            verify(orderRepository).save(any(Order.class));
            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("空商品列表抛出异常")
        void emptyItems_throwsBusinessException() {
            OrderCreateBatchRequest request = new OrderCreateBatchRequest();
            request.setItems(java.util.List.of());

            assertThatThrownBy(() -> orderService.createBatchOrder(10L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("订单必须包含至少一个商品");
        }

        @Test
        @DisplayName("批量订单库存不足抛出异常")
        void insufficientStock_throwsBusinessException() {
            OrderCreateBatchRequest request = new OrderCreateBatchRequest();
            OrderCreateBatchRequest.OrderItemRequest itemReq = new OrderCreateBatchRequest.OrderItemRequest();
            itemReq.setProductId(1L);
            itemReq.setQuantity(10);
            request.setItems(java.util.List.of(itemReq));

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> orderService.createBatchOrder(10L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("库存不足: 1895 Morgan Silver Dollar");
        }
    }

    @Nested
    @DisplayName("支付处理")
    class ProcessPayment {

        @Test
        @DisplayName("有效请求处理支付成功")
        void validRequest_processesPayment() {
            order.setStatus("PENDING_PAYMENT");
            order.setPaymentMethod("stripe");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(paymentService.createPayment(anyLong(), anyString(), any(PaymentRequest.class)))
                    .willReturn(PaymentResponse.builder()
                            .transactionNo("txn-123")
                            .paymentUrl("https://pay.example.com")
                            .status("SUCCESS")
                            .build());

            PaymentResponse response = orderService.processPayment(100L, 10L, "https://return.url", "https://cancel.url");

            assertThat(response).isNotNull();
            assertThat(response.getTransactionNo()).isEqualTo("txn-123");
            assertThat(response.getStatus()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("非本人订单支付抛出异常")
        void notBuyerOrder_throwsBusinessException() {
            order.setStatus("PENDING_PAYMENT");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.processPayment(100L, 99L, "", ""))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("无权操作此订单");
        }

        @Test
        @DisplayName("非待支付状态抛出异常")
        void nonPendingPayment_throwsBusinessException() {
            order.setStatus("PAID");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.processPayment(100L, 10L, "", ""))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("当前状态不允许支付");
        }

        @Test
        @DisplayName("无支付方式抛出异常")
        void noPaymentMethod_throwsBusinessException() {
            order.setStatus("PENDING_PAYMENT");
            order.setPaymentMethod(null);
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.processPayment(100L, 10L, "", ""))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("订单未指定支付方式");
        }
    }

    @Nested
    @DisplayName("买家订单查询")
    class BuyerOrders {

        @Test
        @DisplayName("返回买家订单列表")
        void existingBuyer_returnsOrders() {
            given(orderRepository.findByBuyerIdOrderByCreatedAtDesc(10L)).willReturn(java.util.List.of(order));
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            given(orderLogRepository.findByOrderIdOrderByCreatedAtAsc(anyLong())).willReturn(java.util.List.of());

            var orders = orderService.getBuyerOrders(10L);

            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getOrderNo()).isEqualTo("ORD20260712000001");
        }

        @Test
        @DisplayName("无订单时返回空列表")
        void noOrders_returnsEmptyList() {
            given(orderRepository.findByBuyerIdOrderByCreatedAtDesc(99L)).willReturn(java.util.List.of());

            var orders = orderService.getBuyerOrders(99L);

            assertThat(orders).isEmpty();
        }
    }

    @Nested
    @DisplayName("卖家订单查询")
    class SellerOrders {

        @Test
        @DisplayName("返回卖家订单列表")
        void existingSeller_returnsOrders() {
            given(orderRepository.findBySellerIdOrderByCreatedAtDesc(2L)).willReturn(java.util.List.of(order));
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            given(orderLogRepository.findByOrderIdOrderByCreatedAtAsc(anyLong())).willReturn(java.util.List.of());

            var orders = orderService.getSellerOrders(2L);

            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getSellerId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("无订单时返回空列表")
        void noOrders_returnsEmptyList() {
            given(orderRepository.findBySellerIdOrderByCreatedAtDesc(99L)).willReturn(java.util.List.of());

            var orders = orderService.getSellerOrders(99L);

            assertThat(orders).isEmpty();
        }
    }

    @Nested
    @DisplayName("待收货订单查询")
    class BuyerUnshippedOrders {

        @Test
        @DisplayName("返回未完成订单列表")
        void returnsUnshippedOrders() {
            order.setStatus("PAID");
            given(orderRepository.findByBuyerIdAndStatusInOrderByCreatedAtDesc(eq(10L), anyList()))
                    .willReturn(java.util.List.of(order));
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            given(orderLogRepository.findByOrderIdOrderByCreatedAtAsc(anyLong())).willReturn(java.util.List.of());

            var orders = orderService.getBuyerUnshippedOrders(10L);

            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getStatus()).isEqualTo("PAID");
        }

        @Test
        @DisplayName("全部订单已收货时返回空列表")
        void allCompleted_returnsEmptyList() {
            given(orderRepository.findByBuyerIdAndStatusInOrderByCreatedAtDesc(eq(10L), anyList()))
                    .willReturn(java.util.List.of());

            var orders = orderService.getBuyerUnshippedOrders(10L);

            assertThat(orders).isEmpty();
        }
    }
}
