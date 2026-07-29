package com.orientation.orientationapp.backoffice.analytics.controller;

import com.orientation.orientationapp.backoffice.analytics.dto.response.AnalyticsResponse;
import com.orientation.orientationapp.backoffice.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backoffice/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AnalyticsResponse> getAnalyticsByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(analyticsService.getAnalyticsByTenant(tenantId));
    }
}
