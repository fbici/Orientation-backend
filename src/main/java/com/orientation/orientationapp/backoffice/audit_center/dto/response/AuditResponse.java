package com.orientation.orientationapp.backoffice.audit_center.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditResponse {

    private List<AuditEvent> events;
    private long totalEvents;
    private int page;
    private int size;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditEvent {
        private UUID id;
        private String action;
        private String entityType;
        private String entityId;
        private String userId;
        private String details;
        private String ipAddress;
        private Instant timestamp;
        private String oldValues;
        private String newValues;
    }
}
