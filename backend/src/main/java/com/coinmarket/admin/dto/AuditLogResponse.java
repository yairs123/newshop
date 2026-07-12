package com.coinmarket.admin.dto;

import com.coinmarket.admin.entity.AuditLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "审计日志响应信息")
public class AuditLogResponse {
    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "实体类型")
    private String entityType;

    @Schema(description = "实体ID")
    private Long entityId;

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "变更字段名")
    private String fieldName;

    @Schema(description = "旧值")
    private String oldValue;

    @Schema(description = "新值")
    private String newValue;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "变更摘要")
    private String changeSummary;

    @Schema(description = "创建时间")
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
