package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.AuditLogResponse;
import com.coinmarket.admin.service.AuditService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping("/entity/{entityType}/{entityId}")
    public ApiResponse<PageResponse<AuditLogResponse>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> result = auditService.getEntityHistory(entityType, entityId, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping
    public ApiResponse<PageResponse<AuditLogResponse>> queryLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
        Page<AuditLogResponse> result = auditService.queryLogs(entityType, operation, operatorId, from, to, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }
}
