package com.coinmarket.seller.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.order.repository.OrderRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerApplicationRepository applicationRepository;
    private final SellerProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void submitApplication(Long userId, SellerApplicationRequest request) {
        if (applicationRepository.findByUserIdAndStatus(userId, "PENDING").isPresent()) {
            throw new BusinessException("已有待审核的申请");
        }
        if (applicationRepository.findByUserIdAndStatus(userId, "APPROVED").isPresent()) {
            throw new BusinessException("已经是卖家");
        }

        SellerApplication application = SellerApplication.builder()
                .userId(userId)
                .status("PENDING")
                .idDocumentUrl(request.getIdDocumentUrl())
                .idDocumentType(request.getIdDocumentType())
                .shopName(request.getShopName())
                .shopDescription(request.getShopDescription())
                .build();
        applicationRepository.save(application);
    }

    @Transactional
    public void approveApplication(Long applicationId, Long adminId) {
        SellerApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        app.setStatus("APPROVED");
        app.setReviewedBy(adminId);
        app.setReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        SellerProfile profile = SellerProfile.builder()
                .userId(app.getUserId())
                .shopName(app.getShopName())
                .shopDescription(app.getShopDescription())
                .status("ACTIVE")
                .locked(true)
                .build();
        profileRepository.save(profile);

        User user = userRepository.findById(app.getUserId()).orElseThrow();
        Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                .orElseThrow(() -> new BusinessException("Role not found"));
        user.getRoles().add(sellerRole);
        userRepository.save(user);
    }

    @Transactional
    public void rejectApplication(Long applicationId, Long adminId, String reason) {
        SellerApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        app.setStatus("REJECTED");
        app.setReviewedBy(adminId);
        app.setRejectReason(reason);
        app.setReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);
    }

    public SellerStatusResponse getStatus(Long userId) {
        var application = applicationRepository
                .findFirstByUserIdOrderByCreatedAtDesc(userId);
        var profile = profileRepository.findByUserId(userId);

        SellerStatusResponse response = new SellerStatusResponse();
        response.setHasApplied(application.isPresent());
        application.ifPresent(a -> {
            response.setStatus(a.getStatus());
            response.setRejectReason(a.getRejectReason());
        });
        response.setHasProfile(profile.isPresent());
        profile.ifPresent(p -> response.setLocked(p.isLocked()));
        return response;
    }

    public SellerDashboardResponse getDashboardStats(Long userId) {
        long productCount = productRepository.countBySellerId(userId);
        long pendingOrders = orderRepository.countBySellerIdAndStatus(userId, "PENDING_PAYMENT");
        BigDecimal monthlySales = orderRepository.sumCompletedSalesSince(userId,
                LocalDateTime.now().minusDays(30));
        double averageRating = 0;
        return SellerDashboardResponse.builder()
                .productCount(productCount)
                .pendingOrders(pendingOrders)
                .monthlySales(monthlySales != null ? monthlySales : BigDecimal.ZERO)
                .averageRating(averageRating)
                .build();
    }
}
