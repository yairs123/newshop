package com.coinmarket.finance.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.service.PersonnelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/finance/personnel")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "人员开支", description = "人员开支管理接口")
public class PersonnelController {

    private final PersonnelService personnelService;

    @Operation(summary = "获取人员开支列表")
    @GetMapping
    public ApiResponse<PageResponse<PersonnelExpenseResponse>> listPersonnel(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "payDate"));
        Page<PersonnelExpenseResponse> result = personnelService.listPersonnel(pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @Operation(summary = "新增人员开支")
    @PostMapping
    public ApiResponse<PersonnelExpenseResponse> createPersonnel(
            @Valid @RequestBody PersonnelExpenseRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(personnelService.createPersonnel(request, principal.getId()));
    }

    @Operation(summary = "编辑人员开支")
    @PutMapping("/{id}")
    public ApiResponse<PersonnelExpenseResponse> updatePersonnel(
            @PathVariable Long id, @Valid @RequestBody PersonnelExpenseRequest request) {
        return ApiResponse.success(personnelService.updatePersonnel(id, request));
    }

    @Operation(summary = "删除人员开支")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePersonnel(@PathVariable Long id) {
        personnelService.deletePersonnel(id);
        return ApiResponse.success(null);
    }
}
