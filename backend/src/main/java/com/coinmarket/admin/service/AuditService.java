package com.coinmarket.admin.service;

import com.coinmarket.admin.dto.AuditLogResponse;
import com.coinmarket.admin.entity.AuditLog;
import com.coinmarket.admin.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String entityType, Long entityId, String operation,
                    String fieldName, String oldValue, String newValue,
                    Long operatorId, String operatorName, String changeSummary) {
        AuditLog log = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .operation(operation)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .changeSummary(changeSummary)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional
    public void logCreate(String entityType, Long entityId, Long operatorId, String operatorName) {
        log(entityType, entityId, "CREATE", null, null, null,
                operatorId, operatorName, "Created " + entityType.toLowerCase());
    }

    @Transactional
    public void logDelete(String entityType, Long entityId, Long operatorId, String operatorName) {
        log(entityType, entityId, "DELETE", null, null, null,
                operatorId, operatorName, "Deleted " + entityType.toLowerCase());
    }

    @Transactional
    public void logFieldChanges(String entityType, Long entityId,
                                 Map<String, String[]> fieldChanges,
                                 Long operatorId, String operatorName) {
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, String[]> entry : fieldChanges.entrySet()) {
            String field = entry.getKey();
            String oldVal = entry.getValue()[0];
            String newVal = entry.getValue()[1];
            log(entityType, entityId, "UPDATE", field, oldVal, newVal,
                    operatorId, operatorName,
                    "Changed " + field + " from " + oldVal + " to " + newVal);
            if (summary.length() > 0) summary.append("; ");
            summary.append(field).append(": ").append(oldVal).append(" → ").append(newVal);
        }
    }

    public Page<AuditLogResponse> getEntityHistory(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(AuditLogResponse::from);
    }

    public Page<AuditLogResponse> queryLogs(String entityType, String operation,
                                             Long operatorId, LocalDateTime dateFrom, LocalDateTime dateTo,
                                             Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (entityType != null && !entityType.isEmpty()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (operation != null && !operation.isEmpty()) {
                predicates.add(cb.equal(root.get("operation"), operation));
            }
            if (operatorId != null) {
                predicates.add(cb.equal(root.get("operatorId"), operatorId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return auditLogRepository.findAll(spec, pageable)
                .map(AuditLogResponse::from);
    }
}
