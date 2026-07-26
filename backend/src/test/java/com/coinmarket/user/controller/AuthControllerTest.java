package com.coinmarket.user.controller;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.common.security.JwtAuthenticationFilter;
import com.coinmarket.common.security.JwtTokenProvider;
import com.coinmarket.user.dto.AuthResponse;
import com.coinmarket.user.dto.ForgotPasswordRequest;
import com.coinmarket.user.dto.LoginRequest;
import com.coinmarket.user.dto.RegisterRequest;
import com.coinmarket.user.dto.ResetPasswordRequest;
import com.coinmarket.user.dto.UserProfileResponse;
import com.coinmarket.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void registerShouldReturn201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .preferredLanguage("en")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("test-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(UserProfileResponse.builder()
                        .id(1L)
                        .username("testuser")
                        .email("test@example.com")
                        .build())
                .build();

        given(userService.register(any(RegisterRequest.class))).willReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.token").value("test-token"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
    }

    @Test
    void registerShouldReturn400WhenValidationFails() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("")
                .email("invalid-email")
                .password("12")
                .preferredLanguage("")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void loginShouldReturn200() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("login-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(UserProfileResponse.builder()
                        .id(1L)
                        .username("testuser")
                        .email("test@example.com")
                        .build())
                .build();

        given(userService.login(any(LoginRequest.class))).willReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.token").value("login-token"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    void loginShouldReturn400WhenBadCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        given(userService.login(any(LoginRequest.class)))
                .willThrow(new BusinessException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void forgotPasswordShouldReturn200() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void forgotPasswordShouldReturn400WhenEmailMissing() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void resetPasswordShouldReturn200() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("reset-token")
                .newPassword("newpassword123")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void resetPasswordShouldReturn400WhenTokenMissing() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("")
                .newPassword("newpassword123")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
