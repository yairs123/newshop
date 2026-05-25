package com.coinmarket.admin.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.user.dto.UserProfileResponse;
import com.coinmarket.user.entity.User;
import com.coinmarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .displayName(user.getDisplayName())
                .preferredLanguage(user.getPreferredLanguage())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toSet()))
                .build();
    }
}
