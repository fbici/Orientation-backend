package com.orientation.orientationapp.backoffice.report.service;

import com.orientation.orientationapp.backoffice.report.dto.response.ReportResponse;

public interface ReportService {
    ReportResponse generateReport(String type, String format);
}
