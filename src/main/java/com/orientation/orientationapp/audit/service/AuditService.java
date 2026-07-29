package com.orientation.orientationapp.audit.service;

import com.orientation.orientationapp.common.enums.AuditAction;

public interface AuditService {

    /**
     * Log an audit event
     *
     * @param action     the action performed
     * @param entityType the entity type
     * @param entityId   the entity ID
     * @param details    additional details
     */
    void log(AuditAction action, String entityType, String entityId, String details);

    /**
     * Log an audit event with old and new values
     *
     * @param action     the action performed
     * @param entityType the entity type
     * @param entityId   the entity ID
     * @param oldValues  the old values (JSON string)
     * @param newValues  the new values (JSON string)
     */
    void log(AuditAction action, String entityType, String entityId, String oldValues, String newValues);
}
