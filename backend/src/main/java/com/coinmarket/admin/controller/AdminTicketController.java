package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminTicketService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.user.dto.SupportTicketResponse;
import com.coinmarket.user.entity.SupportTicket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "工单管理", description = "管理员工单管理接口，包括工单查询、回复和状态更新")
public class AdminTicketController {

    private final AdminTicketService adminTicketService;

    @GetMapping
    @Operation(summary = "获取工单列表", description = "查询所有工单，可按状态过滤")
    public ApiResponse<List<SupportTicketResponse>> listTickets(
            @RequestParam(required = false) String status) {
        List<SupportTicket> tickets = adminTicketService.listTickets(status);
        return ApiResponse.success(tickets.stream()
                .map(SupportTicketResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取工单详情", description = "根据ID获取工单详细信息")
    public ApiResponse<SupportTicketResponse> getTicket(@PathVariable Long id) {
        return ApiResponse.success(SupportTicketResponse.fromEntity(adminTicketService.getTicket(id)));
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "回复工单", description = "回复客户提交的工单")
    public ApiResponse<SupportTicketResponse> reply(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        if (reply == null || reply.isBlank()) {
            return ApiResponse.error(400, "Reply is required");
        }
        return ApiResponse.success(SupportTicketResponse.fromEntity(adminTicketService.reply(id, reply)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新工单状态", description = "更新工单处理状态")
    public ApiResponse<SupportTicketResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ApiResponse.error(400, "Status is required");
        }
        return ApiResponse.success(SupportTicketResponse.fromEntity(adminTicketService.updateStatus(id, status)));
    }
}
