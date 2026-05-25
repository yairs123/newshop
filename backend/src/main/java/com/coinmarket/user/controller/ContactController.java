package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.user.dto.SupportTicketRequest;
import com.coinmarket.user.dto.SupportTicketResponse;
import com.coinmarket.user.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final SupportTicketService ticketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SupportTicketResponse> submitContact(@Valid @RequestBody SupportTicketRequest request) {
        return ApiResponse.success(ticketService.createAnonymousTicket(request));
    }
}
