package com.coinmarket.user.dto;

import com.coinmarket.user.entity.SupportTicket;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "工单响应信息")
public class SupportTicketResponse {

    @Schema(description = "工单ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "工单主题")
    private String subject;

    @Schema(description = "工单内容")
    private String message;

    @Schema(description = "工单状态")
    private String status;

    @Schema(description = "工单类型")
    private String ticketType;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "管理员回复")
    private String adminReply;

    @Schema(description = "回复时间")
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
