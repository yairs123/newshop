package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.user.dto.AddressRequest;
import com.coinmarket.user.dto.AddressResponse;
import com.coinmarket.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "地址管理", description = "用户收货地址管理接口")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "获取地址列表", description = "获取当前用户的所有收货地址")
    public ApiResponse<List<AddressResponse>> getAddresses(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(addressService.getUserAddresses(principal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取地址详情", description = "根据ID获取收货地址详细信息")
    public ApiResponse<AddressResponse> getAddress(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(addressService.getAddress(id, principal.getId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增地址", description = "创建新的收货地址")
    public ApiResponse<AddressResponse> createAddress(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {
        return ApiResponse.success(addressService.createAddress(principal.getId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新地址", description = "更新指定的收货地址信息")
    public ApiResponse<AddressResponse> updateAddress(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {
        return ApiResponse.success(addressService.updateAddress(id, principal.getId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "删除指定的收货地址")
    public ApiResponse<Void> deleteAddress(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        addressService.deleteAddress(id, principal.getId());
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "设为默认地址", description = "将指定地址设置为默认收货地址")
    public ApiResponse<Void> setDefault(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        addressService.setDefault(id, principal.getId());
        return ApiResponse.success(null);
    }
}
