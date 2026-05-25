package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.user.dto.SupportTicketResponse;
import com.coinmarket.user.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final SupportTicketService ticketService;

    @GetMapping
    public ApiResponse<List<SupportTicketResponse>> getMessages(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(ticketService.getUserTickets(principal.getId()));
    }

    @GetMapping(params = "type")
    public ApiResponse<List<SupportTicketResponse>> getMessagesByType(
            @CurrentUser UserPrincipal principal,
            @RequestParam String type) {
        return ApiResponse.success(ticketService.getUserTicketsByType(principal.getId(), type));
    }
}
