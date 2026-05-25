package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.Reimbursement;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReimbursementResponse {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String currency;
    private String category;
    private String description;
    private String receiptUrl;
    private String status;
    private Long submitterId;
    private Long approverId;
    private LocalDateTime approvedAt;
    private String rejectReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static ReimbursementResponse from(Reimbursement r) {
        return ReimbursementResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .amount(r.getAmount())
                .currency(r.getCurrency())
                .category(r.getCategory())
                .description(r.getDescription())
                .receiptUrl(r.getReceiptUrl())
                .status(r.getStatus())
                .submitterId(r.getSubmitterId())
                .approverId(r.getApproverId())
                .approvedAt(r.getApprovedAt())
                .rejectReason(r.getRejectReason())
                .paidAt(r.getPaidAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
