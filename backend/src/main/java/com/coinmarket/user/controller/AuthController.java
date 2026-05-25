package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.user.dto.*;
import com.coinmarket.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.generatePasswordResetToken(request.getEmail());
        return ApiResponse.success(null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ApiResponse.success(null);
    }
}
