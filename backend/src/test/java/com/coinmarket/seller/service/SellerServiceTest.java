package com.coinmarket.seller.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.order.entity.Order;
import com.coinmarket.order.repository.OrderRepository;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import com.coinmarket.seller.dto.SellerApplicationRequest;
import com.coinmarket.seller.dto.SellerDashboardResponse;
import com.coinmarket.seller.dto.SellerStatusResponse;
import com.coinmarket.seller.entity.SellerApplication;
import com.coinmarket.seller.entity.SellerProfile;
import com.coinmarket.seller.repository.SellerApplicationRepository;
import com.coinmarket.seller.repository.SellerProfileRepository;
import com.coinmarket.user.entity.Role;
import com.coinmarket.user.entity.User;
import com.coinmarket.user.repository.RoleRepository;
import com.coinmarket.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerApplicationRepository applicationRepository;

    @Mock
    private SellerProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private SellerService sellerService;

    private SellerApplication pendingApplication;
    private SellerApplication approvedApplication;
    private SellerProfile activeProfile;
    private SellerApplicationRequest applicationRequest;
    private User user;
    private Role sellerRole;
    private Order order;

    @BeforeEach
    void setUp() {
        sellerRole = Role.builder().id(2L).name("ROLE_SELLER").build();

        user = User.builder()
                .username("seller")
                .email("seller@test.com")
                .passwordHash("encoded-password")
                .displayName("Seller")
                .enabled(true)
                .roles(new HashSet<>())
                .build();
        user.setId(10L);

        pendingApplication = SellerApplication.builder()
                .userId(10L)
                .status("PENDING")
                .shopName("My Coin Shop")
                .shopDescription("Selling rare coins")
                .idDocumentUrl("https://example.com/id.pdf")
                .idDocumentType("PASSPORT")
                .build();
        pendingApplication.setId(100L);

        approvedApplication = SellerApplication.builder()
                .userId(10L)
                .status("APPROVED")
                .shopName("My Coin Shop")
                .shopDescription("Selling rare coins")
                .build();
        approvedApplication.setId(100L);

        activeProfile = SellerProfile.builder()
                .userId(10L)
                .shopName("My Coin Shop")
                .shopDescription("Selling rare coins")
                .status("ACTIVE")
                .locked(false)
                .build();
        activeProfile.setId(50L);

        applicationRequest = SellerApplicationRequest.builder()
                .shopName("My Coin Shop")
                .shopDescription("Selling rare coins")
                .idDocumentUrl("https://example.com/id.pdf")
                .idDocumentType("PASSPORT")
                .build();

        order = Order.builder()
                .orderNo("ORD20260712000001")
                .buyerId(5L)
                .sellerId(10L)
                .status("COMPLETED")
                .totalAmount(new BigDecimal("1299.99"))
                .currency("USD")
                .build();
        order.setId(200L);
    }

    @Nested
    @DisplayName("提交卖家申请")
    class SubmitApplication {

        @Test
        @DisplayName("新用户提交申请成功")
        void noExistingApplication_submitsSuccessfully() {
            given(applicationRepository.findByUserIdAndStatus(10L, "PENDING")).willReturn(Optional.empty());
            given(applicationRepository.findByUserIdAndStatus(10L, "APPROVED")).willReturn(Optional.empty());
            given(applicationRepository.save(any(SellerApplication.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            sellerService.submitApplication(10L, applicationRequest);

            verify(applicationRepository).save(any(SellerApplication.class));
        }

        @Test
        @DisplayName("已有待审核申请时抛出异常")
        void existingPendingApplication_throwsBusinessException() {
            given(applicationRepository.findByUserIdAndStatus(10L, "PENDING"))
                    .willReturn(Optional.of(pendingApplication));

            assertThatThrownBy(() -> sellerService.submitApplication(10L, applicationRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("已有待审核的申请");
        }

        @Test
        @DisplayName("已是卖家时抛出异常")
        void alreadyApprovedSeller_throwsBusinessException() {
            given(applicationRepository.findByUserIdAndStatus(10L, "PENDING")).willReturn(Optional.empty());
            given(applicationRepository.findByUserIdAndStatus(10L, "APPROVED"))
                    .willReturn(Optional.of(approvedApplication));

            assertThatThrownBy(() -> sellerService.submitApplication(10L, applicationRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("已经是卖家");
        }
    }

    @Nested
    @DisplayName("审核卖家申请")
    class ApproveApplication {

        @Test
        @DisplayName("批准申请成功")
        void validApplication_approvesSuccessfully() {
            given(applicationRepository.findById(100L)).willReturn(Optional.of(pendingApplication));
            given(profileRepository.save(any(SellerProfile.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(userRepository.findById(10L)).willReturn(Optional.of(user));
            given(roleRepository.findByName("ROLE_SELLER")).willReturn(Optional.of(sellerRole));
            given(applicationRepository.save(any(SellerApplication.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            sellerService.approveApplication(100L, 1L);

            verify(applicationRepository).save(any(SellerApplication.class));
            verify(profileRepository).save(any(SellerProfile.class));
            verify(userRepository).save(user);
            assertThat(user.getRoles()).contains(sellerRole);
        }

        @Test
        @DisplayName("申请不存在时抛出异常")
        void unknownApplication_throwsBusinessException() {
            given(applicationRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sellerService.approveApplication(999L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("申请不存在");
        }

        @Test
        @DisplayName("卖家角色不存在时抛出异常")
        void missingSellerRole_throwsBusinessException() {
            given(applicationRepository.findById(100L)).willReturn(Optional.of(pendingApplication));
            given(userRepository.findById(10L)).willReturn(Optional.of(user));
            given(roleRepository.findByName("ROLE_SELLER")).willReturn(Optional.empty());

            assertThatThrownBy(() -> sellerService.approveApplication(100L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Role not found");
        }
    }

    @Nested
    @DisplayName("拒绝卖家申请")
    class RejectApplication {

        @Test
        @DisplayName("拒绝申请成功")
        void validApplication_rejectsSuccessfully() {
            given(applicationRepository.findById(100L)).willReturn(Optional.of(pendingApplication));
            given(applicationRepository.save(any(SellerApplication.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            sellerService.rejectApplication(100L, 1L, "资料不完整");

            verify(applicationRepository).save(any(SellerApplication.class));
            assertThat(pendingApplication.getStatus()).isEqualTo("REJECTED");
            assertThat(pendingApplication.getRejectReason()).isEqualTo("资料不完整");
            assertThat(pendingApplication.getReviewedBy()).isEqualTo(1L);
        }

        @Test
        @DisplayName("申请不存在时抛出异常")
        void unknownApplication_throwsBusinessException() {
            given(applicationRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sellerService.rejectApplication(999L, 1L, "资料不完整"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("申请不存在");
        }
    }

    @Nested
    @DisplayName("查询卖家状态")
    class GetStatus {

        @Test
        @DisplayName("返回已有申请和资料的状态")
        void withApplicationAndProfile_returnsStatus() {
            given(applicationRepository.findFirstByUserIdOrderByCreatedAtDesc(10L))
                    .willReturn(Optional.of(pendingApplication));
            given(profileRepository.findByUserId(10L))
                    .willReturn(Optional.of(activeProfile));

            SellerStatusResponse response = sellerService.getStatus(10L);

            assertThat(response).isNotNull();
            assertThat(response.isHasApplied()).isTrue();
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.isHasProfile()).isTrue();
            assertThat(response.getShopName()).isEqualTo("My Coin Shop");
        }

        @Test
        @DisplayName("只存在申请时返回部分状态")
        void onlyApplication_returnsPartialStatus() {
            given(applicationRepository.findFirstByUserIdOrderByCreatedAtDesc(10L))
                    .willReturn(Optional.of(pendingApplication));
            given(profileRepository.findByUserId(10L))
                    .willReturn(Optional.empty());

            SellerStatusResponse response = sellerService.getStatus(10L);

            assertThat(response.isHasApplied()).isTrue();
            assertThat(response.isHasProfile()).isFalse();
            assertThat(response.isLocked()).isFalse();
        }

        @Test
        @DisplayName("无任何记录时返回空状态")
        void noRecords_returnsEmptyStatus() {
            given(applicationRepository.findFirstByUserIdOrderByCreatedAtDesc(10L))
                    .willReturn(Optional.empty());
            given(profileRepository.findByUserId(10L))
                    .willReturn(Optional.empty());

            SellerStatusResponse response = sellerService.getStatus(10L);

            assertThat(response.isHasApplied()).isFalse();
            assertThat(response.isHasProfile()).isFalse();
            assertThat(response.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("卖家面板统计")
    class GetDashboardStats {

        @Test
        @DisplayName("返回完整面板数据")
        void returnsCompleteDashboardData() {
            given(productRepository.countBySellerId(10L)).willReturn(15L);
            given(orderRepository.countBySellerIdAndStatus(10L, "PENDING_PAYMENT")).willReturn(3L);
            given(orderRepository.sumCompletedSalesSince(anyLong(), any(LocalDateTime.class)))
                    .willReturn(new BigDecimal("5000.00"));
            given(productRepository.countLowStockBySellerId(10L, 3)).willReturn(2L);
            given(orderRepository.countBySellerIdAndStatus(10L, "PAID")).willReturn(1L);
            given(orderRepository.findTop5BySellerIdOrderByCreatedAtDesc(10L))
                    .willReturn(List.of(order));
            given(orderRepository.sumSalesBySellerBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(new BigDecimal("200.00"));

            SellerDashboardResponse response = sellerService.getDashboardStats(10L);

            assertThat(response).isNotNull();
            assertThat(response.getProductCount()).isEqualTo(15L);
            assertThat(response.getPendingOrders()).isEqualTo(3L);
            assertThat(response.getMonthlySales()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(response.getLowStockCount()).isEqualTo(2L);
            assertThat(response.getToShipCount()).isEqualTo(1L);
            assertThat(response.getRecentOrders()).hasSize(1);
            assertThat(response.getWeeklySales()).hasSize(7);
            assertThat(response.getAverageRating()).isZero();
        }

        @Test
        @DisplayName("无销售数据时返回默认值")
        void noSalesData_returnsZeroDefaults() {
            given(productRepository.countBySellerId(10L)).willReturn(0L);
            given(orderRepository.countBySellerIdAndStatus(10L, "PENDING_PAYMENT")).willReturn(0L);
            given(orderRepository.sumCompletedSalesSince(anyLong(), any(LocalDateTime.class)))
                    .willReturn(null);
            given(productRepository.countLowStockBySellerId(10L, 3)).willReturn(0L);
            given(orderRepository.countBySellerIdAndStatus(10L, "PAID")).willReturn(0L);
            given(orderRepository.findTop5BySellerIdOrderByCreatedAtDesc(10L))
                    .willReturn(List.of());
            given(orderRepository.sumSalesBySellerBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(null);

            SellerDashboardResponse response = sellerService.getDashboardStats(10L);

            assertThat(response.getProductCount()).isZero();
            assertThat(response.getMonthlySales()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getRecentOrders()).isEmpty();
            assertThat(response.getWeeklySales()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("更新卖家资料")
    class UpdateProfile {

        @Test
        @DisplayName("更新店铺名称和描述成功")
        void validUpdate_updatesProfile() {
            given(profileRepository.findByUserId(10L)).willReturn(Optional.of(activeProfile));
            given(profileRepository.save(any(SellerProfile.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            sellerService.updateProfile(10L, "New Shop Name", "New description");

            verify(profileRepository).save(activeProfile);
            assertThat(activeProfile.getShopName()).isEqualTo("New Shop Name");
            assertThat(activeProfile.getShopDescription()).isEqualTo("New description");
        }

        @Test
        @DisplayName("只更新店铺名称")
        void onlyShopName_updatesName() {
            given(profileRepository.findByUserId(10L)).willReturn(Optional.of(activeProfile));
            given(profileRepository.save(any(SellerProfile.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            sellerService.updateProfile(10L, "New Shop Name", null);

            assertThat(activeProfile.getShopName()).isEqualTo("New Shop Name");
            assertThat(activeProfile.getShopDescription()).isEqualTo("Selling rare coins");
        }

        @Test
        @DisplayName("资料不存在时抛出异常")
        void noProfile_throwsBusinessException() {
            given(profileRepository.findByUserId(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sellerService.updateProfile(10L, "Name", "Desc"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("卖家资料不存在");
        }

        @Test
        @DisplayName("资料已锁定时抛出异常")
        void lockedProfile_throwsBusinessException() {
            activeProfile.setLocked(true);
            given(profileRepository.findByUserId(10L)).willReturn(Optional.of(activeProfile));

            assertThatThrownBy(() -> sellerService.updateProfile(10L, "Name", "Desc"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("资料已锁定，无法修改");
        }
    }
}
