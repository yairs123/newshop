package com.coinmarket.admin.dto;

import com.coinmarket.admin.entity.AuditLog;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private String entityType;
    private Long entityId;
    private String operation;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Long operatorId;
    private String operatorName;
    private String changeSummary;
    private LocalDateTime createdAt;

    public static AuditLogResponse from(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .operation(log.getOperation())
                .fieldName(log.getFieldName())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .operatorId(log.getOperatorId())
                .operatorName(log.getOperatorName())
                .changeSummary(log.getChangeSummary())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
