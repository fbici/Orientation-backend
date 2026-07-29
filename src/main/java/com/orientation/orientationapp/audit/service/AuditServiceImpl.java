package com.orientation.orientationapp.audit.service;

import com.orientation.orientationapp.audit.model.AuditEvent;
import com.orientation.orientationapp.audit.repository.AuditEventRepository;
import com.orientation.orientationapp.common.enums.AuditAction;
import com.orientation.orientationapp.security.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository auditEventRepository;

    @Override
    @Async
    public void log(AuditAction action, String entityType, String entityId, String details) {
        try {
            AuditEvent event = AuditEvent.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .userId(SecurityUtils.getCurrentUserId().orElse(null))
                    .username(SecurityUtils.getCurrentUsername().orElse("anonymous"))
                    .ipAddress(getClientIp())
                    .userAgent(getUserAgent())
                    .details(details)
                    .build();

            auditEventRepository.save(event);
            log.debug("Audit event logged: {} on {} {}", action, entityType, entityId);
        } catch (Exception ex) {
            log.error("Failed to log audit event", ex);
        }
    }

    @Override
    @Async
    public void log(AuditAction action, String entityType, String entityId, String oldValues, String newValues) {
        try {
            AuditEvent event = AuditEvent.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .userId(SecurityUtils.getCurrentUserId().orElse(null))
                    .username(SecurityUtils.getCurrentUsername().orElse("anonymous"))
                    .ipAddress(getClientIp())
                    .userAgent(getUserAgent())
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .build();

            auditEventRepository.save(event);
            log.debug("Audit event logged: {} on {} {}", action, entityType, entityId);
        } catch (Exception ex) {
            log.error("Failed to log audit event", ex);
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ex) {
            log.debug("Could not get client IP", ex);
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception ex) {
            log.debug("Could not get user agent", ex);
        }
        return "unknown";
    }
}
