package com.orientation.orientationapp.backoffice.monitoring.controller;

import com.orientation.orientationapp.backoffice.monitoring.dto.response.MonitoringResponse;
import com.orientation.orientationapp.backoffice.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backoffice/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<MonitoringResponse> getMonitoring() {
        return ResponseEntity.ok(monitoringService.getMonitoringData());
    }
}
