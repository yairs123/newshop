package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.util.RateLimiter;
import com.coinmarket.user.dto.*;
import com.coinmarket.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证接口，包含登录、注册、密码重置等功能")
public class AuthController {

    private final UserService userService;
    private final RateLimiter rateLimiter;

    private static final int LOGIN_MAX_ATTEMPTS = 10;
    private static final int REGISTER_MAX_ATTEMPTS = 5;
    private static final int PASSWORD_RESET_MAX_ATTEMPTS = 3;
    private static final int WINDOW_SECONDS = 300; // 5 minutes

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                               HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        if (!rateLimiter.isAllowed("register:" + ip, REGISTER_MAX_ATTEMPTS, WINDOW_SECONDS)) {
            return ApiResponse.error("注册请求过于频繁，请稍后再试");
        }
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String rateLimitKey = "login:" + ip;
        if (!rateLimiter.isAllowed(rateLimitKey, LOGIN_MAX_ATTEMPTS, WINDOW_SECONDS)) {
            return ApiResponse.error("登录尝试过于频繁，请稍后再试");
        }

        AuthResponse response = userService.login(request);
        // Reset rate limiter on successful login
        rateLimiter.reset(rateLimitKey);
        return ApiResponse.success(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "发送密码重置邮件到注册邮箱")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                             HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        if (!rateLimiter.isAllowed("forgot-password:" + ip, PASSWORD_RESET_MAX_ATTEMPTS, WINDOW_SECONDS)) {
            return ApiResponse.error("密码重置请求过于频繁，请稍后再试");
        }
        userService.generatePasswordResetToken(request.getEmail());
        return ApiResponse.success(null);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "使用重置令牌设置新密码")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        if (!rateLimiter.isAllowed("reset-password:" + ip, PASSWORD_RESET_MAX_ATTEMPTS, WINDOW_SECONDS)) {
            return ApiResponse.error("密码重置请求过于频繁，请稍后再试");
        }
        userService.resetPassword(request);
        return ApiResponse.success(null);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank() && !xff.equalsIgnoreCase("unknown")) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) {
            return xri;
        }
        return request.getRemoteAddr();
    }
}
