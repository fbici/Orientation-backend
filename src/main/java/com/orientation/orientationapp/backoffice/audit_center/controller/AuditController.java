package com.orientation.orientationapp.backoffice.audit_center.controller;

import com.orientation.orientationapp.backoffice.audit_center.dto.response.AuditResponse;
import com.orientation.orientationapp.backoffice.audit_center.service.AuditCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backoffice/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditCenterService auditCenterService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AuditResponse> getAuditHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditCenterService.getAuditHistory(page, size));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AuditResponse> searchAudit(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditCenterService.searchAudit(q, page, size));
    }
}
