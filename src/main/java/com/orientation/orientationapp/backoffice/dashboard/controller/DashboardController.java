package com.orientation.orientationapp.backoffice.dashboard.controller;

import com.orientation.orientationapp.backoffice.dashboard.dto.response.DashboardResponse;
import com.orientation.orientationapp.backoffice.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backoffice/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getExecutiveDashboard());
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboardByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(dashboardService.getDashboardByTenant(tenantId));
    }
}
