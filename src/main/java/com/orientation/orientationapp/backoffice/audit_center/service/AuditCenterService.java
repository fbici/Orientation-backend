package com.orientation.orientationapp.backoffice.audit_center.service;

import com.orientation.orientationapp.backoffice.audit_center.dto.response.AuditResponse;

public interface AuditCenterService {
    AuditResponse getAuditHistory(int page, int size);
    AuditResponse searchAudit(String query, int page, int size);
}
