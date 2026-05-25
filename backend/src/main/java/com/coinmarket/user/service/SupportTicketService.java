package com.coinmarket.user.service;

import com.coinmarket.user.dto.SupportTicketRequest;
import com.coinmarket.user.dto.SupportTicketResponse;
import com.coinmarket.user.entity.SupportTicket;
import com.coinmarket.user.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;

    public List<SupportTicketResponse> getUserTickets(Long userId) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(SupportTicketResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SupportTicketResponse> getUserTicketsByType(Long userId, String ticketType) {
        return ticketRepository.findByUserIdAndTicketTypeOrderByCreatedAtDesc(userId, ticketType)
                .stream().map(SupportTicketResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupportTicketResponse createTicket(Long userId, SupportTicketRequest request) {
        SupportTicket ticket = SupportTicket.builder()
                .userId(userId)
                .subject(request.getSubject())
                .message(request.getMessage())
                .ticketType(request.getTicketType() != null ? request.getTicketType() : "GENERAL")
                .build();

        SupportTicket saved = ticketRepository.save(ticket);
        return SupportTicketResponse.fromEntity(saved);
    }

    @Transactional
    public SupportTicketResponse createAnonymousTicket(SupportTicketRequest request) {
        SupportTicket ticket = SupportTicket.builder()
                .subject(request.getSubject())
                .message(request.getMessage())
                .ticketType(request.getTicketType() != null ? request.getTicketType() : "GENERAL")
                .build();

        SupportTicket saved = ticketRepository.save(ticket);
        return SupportTicketResponse.fromEntity(saved);
    }
}
