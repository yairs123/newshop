package com.coinmarket.user.repository;

import com.coinmarket.user.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SupportTicket> findByUserIdAndTicketTypeOrderByCreatedAtDesc(Long userId, String ticketType);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(String status);
}
