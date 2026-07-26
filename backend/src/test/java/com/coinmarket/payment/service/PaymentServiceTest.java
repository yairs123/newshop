package com.coinmarket.payment.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private PaymentGateway alipayGateway;
    private PaymentGateway wechatGateway;
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private PaymentResponse successResponse;

    @BeforeEach
    void setUp() {
        alipayGateway = mock(PaymentGateway.class);
        wechatGateway = mock(PaymentGateway.class);

        given(alipayGateway.getMethod()).willReturn("ALIPAY");
        given(wechatGateway.getMethod()).willReturn("WECHAT");

        paymentService = new PaymentService(List.of(alipayGateway, wechatGateway));

        paymentRequest = PaymentRequest.builder()
                .orderNo("ORD20260712000001")
                .amount(new BigDecimal("1299.99"))
                .currency("USD")
                .returnUrl("https://example.com/success")
                .cancelUrl("https://example.com/cancel")
                .build();

        successResponse = PaymentResponse.builder()
                .transactionNo("TXN20260712000001")
                .paymentUrl("https://pay.example.com/123")
                .status("PENDING")
                .build();
    }

    @Nested
    @DisplayName("创建支付")
    class CreatePayment {

        @Test
        @DisplayName("使用支付宝创建支付成功")
        void alipay_createsPayment() {
            given(alipayGateway.createPayment(any(PaymentRequest.class)))
                    .willReturn(successResponse);

            PaymentResponse response = paymentService.createPayment(1L, "ALIPAY", paymentRequest);

            assertThat(response).isNotNull();
            assertThat(response.getTransactionNo()).isEqualTo("TXN20260712000001");
            assertThat(response.getPaymentUrl()).isEqualTo("https://pay.example.com/123");
            assertThat(response.getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("使用微信创建支付成功")
        void wechat_createsPayment() {
            given(wechatGateway.createPayment(any(PaymentRequest.class)))
                    .willReturn(successResponse);

            PaymentResponse response = paymentService.createPayment(1L, "WECHAT", paymentRequest);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("忽略大小写的支付方式查找")
        void caseInsensitiveMethod_findsGateway() {
            given(alipayGateway.createPayment(any(PaymentRequest.class)))
                    .willReturn(successResponse);

            PaymentResponse response = paymentService.createPayment(1L, "alipay", paymentRequest);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("不支持的支付方式抛出异常")
        void unsupportedMethod_throwsBusinessException() {
            assertThatThrownBy(() -> paymentService.createPayment(1L, "CREDIT_CARD", paymentRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("不支持的支付方式: CREDIT_CARD");
        }

        @Test
        @DisplayName("空字符串支付方式抛出异常")
        void emptyMethod_throwsBusinessException() {
            assertThatThrownBy(() -> paymentService.createPayment(1L, "", paymentRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("不支持的支付方式: ");
        }
    }

    @Nested
    @DisplayName("查询支付")
    class QueryPayment {

        @Test
        @DisplayName("查询支付宝支付状态成功")
        void alipay_queriesPayment() {
            PaymentResponse queryResponse = PaymentResponse.builder()
                    .transactionNo("TXN20260712000001")
                    .status("SUCCESS")
                    .build();
            given(alipayGateway.queryPayment("TXN20260712000001"))
                    .willReturn(queryResponse);

            PaymentResponse response = paymentService.queryPayment(1L, "ALIPAY", "TXN20260712000001");

            assertThat(response.getStatus()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("查询不支持的支付方式抛出异常")
        void unsupportedMethod_throwsBusinessException() {
            assertThatThrownBy(() -> paymentService.queryPayment(1L, "BITCOIN", "TXN123"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("不支持的支付方式: BITCOIN");
        }
    }

    @Nested
    @DisplayName("退款")
    class Refund {

        @Test
        @DisplayName("支付宝退款成功")
        void alipay_refundsSuccessfully() {
            PaymentResponse refundResponse = PaymentResponse.builder()
                    .transactionNo("TXN20260712000001")
                    .status("REFUNDED")
                    .build();
            given(alipayGateway.refund("TXN20260712000001", new BigDecimal("1299.99")))
                    .willReturn(refundResponse);

            PaymentResponse response = paymentService.refund(1L, "ALIPAY", "TXN20260712000001", new BigDecimal("1299.99"));

            assertThat(response.getStatus()).isEqualTo("REFUNDED");
        }

        @Test
        @DisplayName("退款不支持的支付方式抛出异常")
        void unsupportedMethod_throwsBusinessException() {
            assertThatThrownBy(() -> paymentService.refund(1L, "PAYPAL", "TXN123", BigDecimal.TEN))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("不支持的支付方式: PAYPAL");
        }
    }

    @Nested
    @DisplayName("获取可用支付方式")
    class GetAvailableMethods {

        @Test
        @DisplayName("返回所有已注册的支付方式")
        void returnsRegisteredMethods() {
            List<String> methods = paymentService.getAvailableMethods();

            assertThat(methods).containsExactlyInAnyOrder("ALIPAY", "WECHAT");
        }

        @Test
        @DisplayName("返回不可变列表")
        void returnsImmutableList() {
            List<String> methods = paymentService.getAvailableMethods();

            assertThatThrownBy(() -> methods.add("NEW_METHOD"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
