package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.user.dto.SupportTicketResponse;
import com.coinmarket.user.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "用户消息和工单管理接口")
public class MessageController {

    private final SupportTicketService ticketService;

    @GetMapping
    @Operation(summary = "获取消息列表", description = "获取当前用户的所有消息或工单")
    public ApiResponse<List<SupportTicketResponse>> getMessages(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(ticketService.getUserTickets(principal.getId()));
    }

    @GetMapping(params = "type")
    @Operation(summary = "按类型获取消息", description = "根据类型筛选获取用户的消息或工单")
    public ApiResponse<List<SupportTicketResponse>> getMessagesByType(
            @CurrentUser UserPrincipal principal,
            @RequestParam String type) {
        return ApiResponse.success(ticketService.getUserTicketsByType(principal.getId(), type));
    }
}
