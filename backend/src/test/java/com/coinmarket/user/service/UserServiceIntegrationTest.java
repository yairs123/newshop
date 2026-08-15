package com.coinmarket.user.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.user.dto.AuthResponse;
import com.coinmarket.user.dto.LoginRequest;
import com.coinmarket.user.dto.RegisterRequest;
import com.coinmarket.user.entity.Role;
import com.coinmarket.user.repository.RoleRepository;
import com.coinmarket.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Service-level integration test verifying that {@link UserService} works end-to-end
 * against the real JPA layer backed by H2 (test profile). The {@code @Transactional}
 * test rolls back all persisted data after each test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService Integration Test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void seedBuyerRole() {
        if (roleRepository.findByName("ROLE_BUYER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_BUYER").build());
        }
    }

    private RegisterRequest registerRequest(String username, String email) {
        return RegisterRequest.builder()
                .username(username)
                .email(email)
                .password("Password123")
                .displayName("Integration User")
                .preferredLanguage("en")
                .build();
    }

    @Test
    @DisplayName("注册用户持久化到数据库")
    void register_persistsUserToDatabase() {
        AuthResponse response = userService.register(registerRequest("ituser1", "ituser1@test.com"));

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUser().getUsername()).isEqualTo("ituser1");
        assertThat(response.getUser().getRoles()).contains("ROLE_BUYER");

        // Verify the row really hit the database
        assertThat(userRepository.existsByUsername("ituser1")).isTrue();
        assertThat(userRepository.findByUsername("ituser1")).isPresent();
        assertThat(userRepository.findByUsername("ituser1").orElseThrow().isEnabled()).isTrue();
    }

    @Test
    @DisplayName("登录从数据库读取用户并校验密码")
    void login_readsUserFromDatabase() {
        userService.register(registerRequest("itlogin", "itlogin@test.com"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("itlogin");
        loginRequest.setPassword("Password123");

        AuthResponse response = userService.login(loginRequest);

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getUsername()).isEqualTo("itlogin");
    }

    @Test
    @DisplayName("登录时密码错误抛出401")
    void login_wrongPassword_throws() {
        userService.register(registerRequest("itbadpw", "itbadpw@test.com"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("itbadpw");
        loginRequest.setPassword("WrongPass123");

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("重复用户名注册被拒绝")
    void register_duplicateUsername_rejected() {
        userService.register(registerRequest("itdup", "itdup@test.com"));

        assertThatThrownBy(() -> userService.register(registerRequest("itdup", "other@test.com")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Username already exists");

        // Only one row persisted
        assertThat(userRepository.existsByUsername("itdup")).isTrue();
    }

    @Test
    @DisplayName("重复邮箱注册被拒绝")
    void register_duplicateEmail_rejected() {
        userService.register(registerRequest("itmail1", "dup@test.com"));

        assertThatThrownBy(() -> userService.register(registerRequest("itmail2", "dup@test.com")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email already exists");
    }
}
