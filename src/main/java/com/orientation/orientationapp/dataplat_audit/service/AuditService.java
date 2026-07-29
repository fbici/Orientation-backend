package com.orientation.orientationapp.dataplat_audit.service;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportMetadata;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * Service for audit trail of all import operations.
 */
public interface AuditService {

    /**
     * Record an audit event.
     *
     * @param action  the action performed
     * @param details the event details
     * @param userId  the user who performed the action
     */
    void record(String action, String details, String userId);

    /**
     * Record an import audit event.
     *
     * @param metadata the import metadata
     */
    void recordImport(ImportMetadata metadata);

    /**
     * Get audit trail for an import.
     *
     * @param importId the import ID
     * @return list of audit events
     */
    List<AuditEvent> getAuditTrail(UUID importId);

    /**
     * Get audit events for a user.
     *
     * @param userId the user ID
     * @return list of audit events
     */
    List<AuditEvent> getAuditTrail(String userId);

    /**
     * Audit event model.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class AuditEvent {
        private UUID id;
        private String action;
        private String details;
        private String userId;
        private UUID importId;
        private java.time.Instant timestamp;
    }
}
