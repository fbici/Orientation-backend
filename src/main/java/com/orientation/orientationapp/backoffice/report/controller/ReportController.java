package com.orientation.orientationapp.backoffice.report.controller;

import com.orientation.orientationapp.backoffice.report.dto.response.ReportResponse;
import com.orientation.orientationapp.backoffice.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backoffice/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/generate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> generateReport(
            @RequestParam String type,
            @RequestParam(defaultValue = "json") String format) {
        return ResponseEntity.ok(reportService.generateReport(type, format));
    }
}
