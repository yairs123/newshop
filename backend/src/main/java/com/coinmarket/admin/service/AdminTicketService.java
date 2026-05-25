package com.coinmarket.admin.service;

import com.coinmarket.user.entity.SupportTicket;
import com.coinmarket.user.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTicketService {

    private final SupportTicketRepository ticketRepository;

    public List<SupportTicket> listTickets(String status) {
        if (status != null && !status.isBlank()) {
            return ticketRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    public SupportTicket getTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + id));
    }

    @Transactional
    public SupportTicket reply(Long id, String reply) {
        SupportTicket ticket = getTicket(id);
        ticket.setAdminReply(reply);
        ticket.setRepliedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public SupportTicket updateStatus(Long id, String status) {
        SupportTicket ticket = getTicket(id);
        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }
}
