package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.user.dto.SupportTicketRequest;
import com.coinmarket.user.dto.SupportTicketResponse;
import com.coinmarket.user.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "联系我们", description = "意见反馈和客户联系接口")
public class ContactController {

    private final SupportTicketService ticketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "提交反馈", description = "提交意见反馈或联系请求")
    public ApiResponse<SupportTicketResponse> submitContact(@Valid @RequestBody SupportTicketRequest request) {
        return ApiResponse.success(ticketService.createAnonymousTicket(request));
    }
}
