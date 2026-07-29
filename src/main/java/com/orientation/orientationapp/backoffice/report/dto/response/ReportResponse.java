package com.orientation.orientationapp.backoffice.report.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    private String reportType;
    private String format;
    private byte[] data;
    private Map<String, Object> metadata;
}
