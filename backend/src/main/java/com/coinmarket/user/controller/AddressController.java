package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.user.dto.AddressRequest;
import com.coinmarket.user.dto.AddressResponse;
import com.coinmarket.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse<List<AddressResponse>> getAddresses(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(addressService.getUserAddresses(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<AddressResponse> getAddress(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(addressService.getAddress(id, principal.getId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> createAddress(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {
        return ApiResponse.success(addressService.createAddress(principal.getId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {
        return ApiResponse.success(addressService.updateAddress(id, principal.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        addressService.deleteAddress(id, principal.getId());
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<Void> setDefault(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        addressService.setDefault(id, principal.getId());
        return ApiResponse.success(null);
    }
}
