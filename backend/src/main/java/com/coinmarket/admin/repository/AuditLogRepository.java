package com.coinmarket.admin.repository;

import com.coinmarket.admin.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:operation IS NULL OR a.operation = :operation) AND " +
           "(:operatorId IS NULL OR a.operatorId = :operatorId) AND " +
           "(:dateFrom IS NULL OR a.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR a.createdAt <= :dateTo) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> queryLogs(
            @Param("entityType") String entityType,
            @Param("operation") String operation,
            @Param("operatorId") Long operatorId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
}
