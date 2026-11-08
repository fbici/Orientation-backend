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
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                "Select a saved decision and use /ai/export/{pdf,csv,excel}/{id}; aggregate reports are unavailable without measured data");
    }
}
