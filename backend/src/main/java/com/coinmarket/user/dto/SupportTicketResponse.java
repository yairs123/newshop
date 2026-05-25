package com.coinmarket.user.dto;

import com.coinmarket.user.entity.SupportTicket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketResponse {

    private Long id;
    private Long userId;
    private String subject;
    private String message;
    private String status;
    private String ticketType;
    private LocalDateTime createdAt;
    private String adminReply;
    private LocalDateTime repliedAt;

    public static SupportTicketResponse fromEntity(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .userId(ticket.getUserId())
                .subject(ticket.getSubject())
                .message(ticket.getMessage())
                .status(ticket.getStatus())
                .ticketType(ticket.getTicketType())
                .createdAt(ticket.getCreatedAt())
                .adminReply(ticket.getAdminReply())
                .repliedAt(ticket.getRepliedAt())
                .build();
    }
}
