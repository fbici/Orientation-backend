package com.orientation.orientationapp.audit.repository;

import com.orientation.orientationapp.audit.model.AuditEvent;
import com.orientation.orientationapp.common.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    Page<AuditEvent> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    Page<AuditEvent> findByUserId(String userId, Pageable pageable);

    Page<AuditEvent> findByAction(AuditAction action, Pageable pageable);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.createdAt BETWEEN :startDate AND :endDate")
    Page<AuditEvent> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.entityType = :entityType AND ae.createdAt BETWEEN :startDate AND :endDate")
    List<AuditEvent> findByEntityTypeAndDateRange(
            @Param("entityType") String entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
