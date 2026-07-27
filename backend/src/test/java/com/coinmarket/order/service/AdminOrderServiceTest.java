package com.coinmarket.order.service;

import com.coinmarket.admin.service.AdminOrderService;
import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.common.notification.NotificationService;
import com.coinmarket.order.dto.AdminOrderUpdateRequest;
import com.coinmarket.order.dto.OrderResponse;
import com.coinmarket.order.entity.Order;
import com.coinmarket.order.entity.OrderEditLog;
import com.coinmarket.order.repository.OrderEditLogRepository;
import com.coinmarket.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderEditLogRepository orderEditLogRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminOrderService adminOrderService;

    private Order order;
    private Order paidOrder;
    private OrderEditLog editLog;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .orderNo("ORD20260712000001")
                .buyerId(10L)
                .sellerId(2L)
                .status("PENDING_PAYMENT")
                .totalAmount(new BigDecimal("1299.99"))
                .currency("USD")
                .shippingAddress("123 Main St")
                .build();
        order.setId(100L);

        paidOrder = Order.builder()
                .orderNo("ORD20260712000002")
                .buyerId(10L)
                .sellerId(2L)
                .status("PAID")
                .totalAmount(new BigDecimal("500.00"))
                .currency("USD")
                .build();
        paidOrder.setId(101L);

        editLog = OrderEditLog.builder()
                .orderId(100L)
                .fieldName("status")
                .oldValue("PENDING_PAYMENT")
                .newValue("PAID")
                .reason("管理员标记已支付")
                .operator("管理员")
                .build();
        editLog.setId(1L);
    }

    @Nested
    @DisplayName("订单列表")
    class ListOrders {

        @Test
        @DisplayName("返回分页订单列表")
        void returnsPagedOrders() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);
            given(orderRepository.findAll(pageable)).willReturn(orderPage);

            Page<OrderResponse> result = adminOrderService.listOrders(pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getOrderNo()).isEqualTo("ORD20260712000001");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo("PENDING_PAYMENT");
        }

        @Test
        @DisplayName("无订单时返回空列表")
        void noOrders_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(orderRepository.findAll(pageable)).willReturn(emptyPage);

            Page<OrderResponse> result = adminOrderService.listOrders(pageable);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("获取订单详情")
    class GetOrder {

        @Test
        @DisplayName("返回带编辑日志的订单")
        void existingOrder_returnsOrderWithEditLogs() {
            OrderResponse orderResponse = OrderResponse.builder()
                    .id(100L)
                    .orderNo("ORD20260712000001")
                    .status("PENDING_PAYMENT")
                    .totalAmount(new BigDecimal("1299.99"))
                    .build();
            given(orderService.getOrder(100L)).willReturn(orderResponse);
            given(orderEditLogRepository.findByOrderIdOrderByCreatedAtAsc(100L))
                    .willReturn(List.of(editLog));

            OrderResponse result = adminOrderService.getOrder(100L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getEditLogs()).hasSize(1);
            assertThat(result.getEditLogs().get(0).getFieldName()).isEqualTo("status");
        }

        @Test
        @DisplayName("无编辑日志的订单")
        void noEditLogs_returnsOrderWithEmptyLogs() {
            OrderResponse orderResponse = OrderResponse.builder()
                    .id(100L)
                    .orderNo("ORD20260712000001")
                    .status("PENDING_PAYMENT")
                    .build();
            given(orderService.getOrder(100L)).willReturn(orderResponse);
            given(orderEditLogRepository.findByOrderIdOrderByCreatedAtAsc(100L))
                    .willReturn(List.of());

            OrderResponse result = adminOrderService.getOrder(100L);

            assertThat(result.getEditLogs()).isEmpty();
        }
    }

    @Nested
    @DisplayName("标记已支付")
    class MarkPaid {

        @Test
        @DisplayName("待支付订单标记成功")
        void pendingPaymentOrder_marksAsPaid() {
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderEditLogRepository.save(any(OrderEditLog.class))).willReturn(editLog);

            adminOrderService.markPaid(100L);

            assertThat(order.getStatus()).isEqualTo("PAID");
            assertThat(order.getPaidAt()).isNotNull();
            verify(orderEditLogRepository).save(any(OrderEditLog.class));
        }

        @Test
        @DisplayName("订单不存在抛出异常")
        void unknownOrder_throwsBusinessException() {
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminOrderService.markPaid(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("订单不存在");
        }

        @Test
        @DisplayName("非待支付状态抛出异常")
        void nonPendingPayment_throwsBusinessException() {
            order.setStatus("SHIPPED");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> adminOrderService.markPaid(100L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("当前状态不允许标记已支付");
        }
    }

    @Nested
    @DisplayName("取消订单")
    class CancelOrder {

        @Test
        @DisplayName("待支付订单取消成功")
        void pendingPaymentOrder_cancelsSuccessfully() {
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderEditLogRepository.save(any(OrderEditLog.class))).willReturn(editLog);

            adminOrderService.cancelOrder(100L);

            assertThat(order.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("已支付订单也可取消")
        void paidOrder_cancelsSuccessfully() {
            given(orderRepository.findById(101L)).willReturn(Optional.of(paidOrder));
            given(orderRepository.save(any(Order.class))).willReturn(paidOrder);
            given(orderEditLogRepository.save(any(OrderEditLog.class))).willReturn(editLog);

            adminOrderService.cancelOrder(101L);

            assertThat(paidOrder.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("已发货订单不能取消")
        void shippedOrder_throwsBusinessException() {
            order.setStatus("SHIPPED");
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> adminOrderService.cancelOrder(100L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("当前状态不允许取消");
        }

        @Test
        @DisplayName("订单不存在抛出异常")
        void unknownOrder_throwsBusinessException() {
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminOrderService.cancelOrder(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("订单不存在");
        }
    }

    @Nested
    @DisplayName("强制完成")
    class ForceComplete {

        @Test
        @DisplayName("委托给OrderService完成")
        void delegatesToOrderService() {
            adminOrderService.forceComplete(100L);

            verify(orderService).forceCompleteOrder(100L);
        }
    }

    @Nested
    @DisplayName("发货")
    class ShipOrder {

        @Test
        @DisplayName("已支付订单发货成功")
        void paidOrder_shipsSuccessfully() {
            given(orderRepository.findById(101L)).willReturn(Optional.of(paidOrder));
            given(orderRepository.save(any(Order.class))).willReturn(paidOrder);
            given(orderEditLogRepository.save(any(OrderEditLog.class))).willReturn(editLog);

            adminOrderService.shipOrder(101L, "1Z999AA10123456784", "UPS");

            assertThat(paidOrder.getStatus()).isEqualTo("SHIPPED");
            assertThat(paidOrder.getTrackingNumber()).isEqualTo("1Z999AA10123456784");
            assertThat(paidOrder.getTrackingCompany()).isEqualTo("UPS");
        }

        @Test
        @DisplayName("非已支付状态抛出异常")
        void nonPaidStatus_throwsBusinessException() {
            given(orderRepository.findById(100L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> adminOrderService.shipOrder(100L, "TN123", "UPS"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("当前状态不允许发货");
        }

        @Test
        @DisplayName("订单不存在抛出异常")
        void unknownOrder_throwsBusinessException() {
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminOrderService.shipOrder(999L, "TN123", "UPS"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("订单不存在");
        }
    }

    @Nested
    @DisplayName("更新订单")
    class UpdateOrder {

        @Test
        @DisplayName("更新多个字段成功")
        void multipleFields_updatesSuccessfully() {
            AdminOrderUpdateRequest request = AdminOrderUpdateRequest.builder()
                    .paidAt(LocalDateTime.of(2026, 7, 15, 10, 0))
                    .trackingNumber("TN123456")
                    .trackingCompany("FedEx")
                    .adminNote("VIP客户")
                    .reason("客户要求")
                    .build();

            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderEditLogRepository.save(any(OrderEditLog.class))).willReturn(editLog);

            adminOrderService.updateOrder(100L, request);

            assertThat(order.getPaidAt()).isNotNull();
            assertThat(order.getTrackingNumber()).isEqualTo("TN123456");
            assertThat(order.getTrackingCompany()).isEqualTo("FedEx");
            assertThat(order.getAdminNote()).isEqualTo("VIP客户");
            verify(orderEditLogRepository, org.mockito.Mockito.times(4)).save(any(OrderEditLog.class));
        }

        @Test
        @DisplayName("只更新管理员备注")
        void onlyAdminNote_updatesNote() {
            AdminOrderUpdateRequest request = AdminOrderUpdateRequest.builder()
                    .adminNote("测试备注")
                    .build();

            given(orderRepository.findById(100L)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderEditLogRepository.save(any(OrderEditLog.class))).willReturn(editLog);

            adminOrderService.updateOrder(100L, request);

            assertThat(order.getAdminNote()).isEqualTo("测试备注");
        }

        @Test
        @DisplayName("订单不存在抛出异常")
        void unknownOrder_throwsBusinessException() {
            AdminOrderUpdateRequest request = AdminOrderUpdateRequest.builder()
                    .adminNote("test")
                    .build();

            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminOrderService.updateOrder(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("订单不存在");
        }
    }
}
