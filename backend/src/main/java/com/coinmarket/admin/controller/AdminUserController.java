package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminUserService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResponse<UserProfileResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        var profiles = adminUserService.listUsers(pageable);
        return ApiResponse.success(PageResponse.from(profiles));
    }

    @PutMapping("/{id}/toggle-status")
    public ApiResponse<Void> toggleUserStatus(@PathVariable Long id) {
        adminUserService.toggleUserStatus(id);
        return ApiResponse.success(null);
    }
}
