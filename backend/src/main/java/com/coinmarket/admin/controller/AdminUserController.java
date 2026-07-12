package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminUserService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "后台用户管理", description = "管理员用户管理接口，包括用户列表查询、账号启用/禁用等操作")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "获取用户列表", description = "分页查询所有注册用户")
    public ApiResponse<PageResponse<UserProfileResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        var profiles = adminUserService.listUsers(pageable);
        return ApiResponse.success(PageResponse.from(profiles));
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "切换用户状态", description = "启用或禁用用户账号")
    public ApiResponse<Void> toggleUserStatus(@PathVariable Long id) {
        adminUserService.toggleUserStatus(id);
        return ApiResponse.success(null);
    }
}
