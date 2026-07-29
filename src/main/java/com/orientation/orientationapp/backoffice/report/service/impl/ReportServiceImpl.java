package com.orientation.orientationapp.backoffice.report.service.impl;

import com.orientation.orientationapp.backoffice.report.dto.response.ReportResponse;
import com.orientation.orientationapp.backoffice.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public ReportResponse generateReport(String type, String format) {
        return ReportResponse.builder()
                .reportType(type)
                .format(format)
                .metadata(Map.of("generatedAt", java.time.Instant.now().toString()))
                .build();
    }
}
