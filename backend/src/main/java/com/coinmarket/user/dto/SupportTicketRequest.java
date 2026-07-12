package com.coinmarket.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "工单请求参数")
public class SupportTicketRequest {

    @NotBlank(message = "Subject is required")
    @Schema(description = "工单主题")
    private String subject;

    @NotBlank(message = "Message is required")
    @Schema(description = "工单内容")
    private String message;

    @Schema(description = "工单类型")
    private String ticketType;
}
