package com.coinmarket.user.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.common.security.JwtTokenProvider;
import com.coinmarket.user.dto.AuthResponse;
import com.coinmarket.user.dto.LoginRequest;
import com.coinmarket.user.dto.RegisterRequest;
import com.coinmarket.user.entity.Role;
import com.coinmarket.user.entity.User;
import com.coinmarket.user.repository.PasswordResetTokenRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private UserService userService;

    private Role adminRole;
    private Role buyerRole;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().id(3L).name("ROLE_ADMIN").build();
        buyerRole = Role.builder().id(1L).name("ROLE_BUYER").build();

        adminUser = User.builder()
                .username("admin")
                .email("admin@test.com")
                .passwordHash("encoded-admin-123")
                .displayName("Admin")
                .preferredLanguage("en")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();
        adminUser.setId(1L);
    }

    @Nested
    @DisplayName("登录")
    class Login {

        @Test
        @DisplayName("有效凭据登录成功")
        void validCredentials_returnsAuthResponse() {
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("123");

            given(userRepository.findByUsername("admin")).willReturn(Optional.of(adminUser));
            given(passwordEncoder.matches("123", "encoded-admin-123")).willReturn(true);
            given(jwtTokenProvider.generateToken(1L, "admin", java.util.List.of("ROLE_ADMIN")))
                    .willReturn("jwt-token");

            AuthResponse response = userService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getUsername()).isEqualTo("admin");
            assertThat(response.getUser().getRoles()).contains("ROLE_ADMIN");
        }

        @Test
        @DisplayName("密码错误抛出401异常")
        void wrongPassword_throwsBusinessException() {
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("wrong");

            given(userRepository.findByUsername("admin")).willReturn(Optional.of(adminUser));
            given(passwordEncoder.matches("wrong", "encoded-admin-123")).willReturn(false);

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Invalid credentials");
        }

        @Test
        @DisplayName("用户名不存在抛出401异常")
        void unknownUsername_throwsBusinessException() {
            LoginRequest request = new LoginRequest();
            request.setUsername("unknown");
            request.setPassword("123");

            given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Invalid credentials");
        }

        @Test
        @DisplayName("账号禁用抛出403异常")
        void disabledAccount_throwsBusinessException() {
            adminUser.setEnabled(false);
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("123");

            given(userRepository.findByUsername("admin")).willReturn(Optional.of(adminUser));
            given(passwordEncoder.matches("123", "encoded-admin-123")).willReturn(true);

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Account is disabled");
        }
    }

    @Nested
    @DisplayName("注册")
    class Register {

        @Test
        @DisplayName("新用户注册成功")
        void validRequest_createsUser() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("new@test.com");
            request.setPassword("password123");

            given(userRepository.existsByUsername("newuser")).willReturn(false);
            given(userRepository.existsByEmail("new@test.com")).willReturn(false);
            given(roleRepository.findByName("ROLE_BUYER")).willReturn(Optional.of(buyerRole));
            given(passwordEncoder.encode("password123")).willReturn("encoded-new-password");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                saved.setId(99L);
                return saved;
            });
            given(jwtTokenProvider.generateToken(99L, "newuser", java.util.List.of("ROLE_BUYER")))
                    .willReturn("jwt-new-user");

            AuthResponse response = userService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-new-user");
            assertThat(response.getUser().getUsername()).isEqualTo("newuser");
            assertThat(response.getUser().getRoles()).contains("ROLE_BUYER");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("用户名重复抛出409")
        void duplicateUsername_throwsBusinessException() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existing");
            request.setEmail("new@test.com");
            request.setPassword("password123");

            given(userRepository.existsByUsername("existing")).willReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Username already exists");
        }

        @Test
        @DisplayName("邮箱重复抛出409")
        void duplicateEmail_throwsBusinessException() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("used@test.com");
            request.setPassword("password123");

            given(userRepository.existsByUsername("newuser")).willReturn(false);
            given(userRepository.existsByEmail("used@test.com")).willReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email already exists");
        }
    }

    @Nested
    @DisplayName("个人信息")
    class Profile {

        @Test
        @DisplayName("获取用户信息成功")
        void existingUser_returnsProfile() {
            given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));

            var profile = userService.getProfile(1L);

            assertThat(profile).isNotNull();
            assertThat(profile.getUsername()).isEqualTo("admin");
            assertThat(profile.getEmail()).isEqualTo("admin@test.com");
            assertThat(profile.getDisplayName()).isEqualTo("Admin");
            assertThat(profile.getRoles()).contains("ROLE_ADMIN");
        }

        @Test
        @DisplayName("用户不存在抛出异常")
        void unknownUser_throwsBusinessException() {
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getProfile(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("User not found");
        }
    }
}
