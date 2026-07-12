package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.Reimbursement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "报销记录响应数据")
public class ReimbursementResponse {
    @Schema(description = "报销ID")
    private Long id;

    @Schema(description = "报销标题")
    private String title;

    @Schema(description = "报销金额")
    private BigDecimal amount;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "报销类别")
    private String category;

    @Schema(description = "报销说明")
    private String description;

    @Schema(description = "收据/凭证图片URL")
    private String receiptUrl;

    @Schema(description = "状态（PENDING/APPROVED/REJECTED/PAID）")
    private String status;

    @Schema(description = "提交人ID")
    private Long submitterId;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批时间")
    private LocalDateTime approvedAt;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "支付时间")
    private LocalDateTime paidAt;

    @Schema(description = "创建时间")
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
