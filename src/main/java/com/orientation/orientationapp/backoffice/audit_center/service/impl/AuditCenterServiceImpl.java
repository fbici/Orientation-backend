package com.orientation.orientationapp.backoffice.audit_center.service.impl;

import com.orientation.orientationapp.backoffice.audit_center.dto.response.AuditResponse;
import com.orientation.orientationapp.backoffice.audit_center.service.AuditCenterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AuditCenterServiceImpl implements AuditCenterService {

    @Override
    public AuditResponse getAuditHistory(int page, int size) {
        return AuditResponse.builder()
                .events(List.of())
                .totalEvents(0)
                .page(page)
                .size(size)
                .build();
    }

    @Override
    public AuditResponse searchAudit(String query, int page, int size) {
        return getAuditHistory(page, size);
    }
}
