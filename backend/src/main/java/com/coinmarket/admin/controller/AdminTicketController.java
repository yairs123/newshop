package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminTicketService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.user.dto.SupportTicketResponse;
import com.coinmarket.user.entity.SupportTicket;
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
public class AdminTicketController {

    private final AdminTicketService adminTicketService;

    @GetMapping
    public ApiResponse<List<SupportTicketResponse>> listTickets(
            @RequestParam(required = false) String status) {
        List<SupportTicket> tickets = adminTicketService.listTickets(status);
        return ApiResponse.success(tickets.stream()
                .map(SupportTicketResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SupportTicketResponse> getTicket(@PathVariable Long id) {
        return ApiResponse.success(SupportTicketResponse.fromEntity(adminTicketService.getTicket(id)));
    }

    @PostMapping("/{id}/reply")
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
