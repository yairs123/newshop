package com.coinmarket.user.entity;

import com.coinmarket.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets", schema = "coin_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "ticket_type", nullable = false, length = 30)
    @Builder.Default
    private String ticketType = "GENERAL";

    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;
}
