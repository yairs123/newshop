package com.coinmarket.user.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.user.dto.PaymentMethodRequest;
import com.coinmarket.user.dto.PaymentMethodResponse;
import com.coinmarket.user.entity.UserPaymentMethod;
import com.coinmarket.user.repository.UserPaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final UserPaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethodResponse> getUserPaymentMethods(Long userId) {
        return paymentMethodRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(PaymentMethodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentMethodResponse createPaymentMethod(Long userId, PaymentMethodRequest request) {
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultFlag(userId);
        }

        UserPaymentMethod pm = UserPaymentMethod.builder()
                .userId(userId)
                .methodType(request.getMethodType())
                .provider(request.getProvider())
                .accountLastFour(request.getAccountLastFour())
                .expiryDate(request.getExpiryDate())
                .cardholderName(request.getCardholderName())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();

        UserPaymentMethod saved = paymentMethodRepository.save(pm);
        return PaymentMethodResponse.fromEntity(saved);
    }

    @Transactional
    public void deletePaymentMethod(Long id, Long userId) {
        UserPaymentMethod existing = paymentMethodRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Payment method not found"));
        paymentMethodRepository.delete(existing);
    }

    @Transactional
    public void setDefault(Long id, Long userId) {
        UserPaymentMethod pm = paymentMethodRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Payment method not found"));
        clearDefaultFlag(userId);
        pm.setIsDefault(true);
        paymentMethodRepository.save(pm);
    }

    private void clearDefaultFlag(Long userId) {
        List<UserPaymentMethod> defaults = paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId);
        defaults.forEach(pm -> pm.setIsDefault(false));
    }
}
