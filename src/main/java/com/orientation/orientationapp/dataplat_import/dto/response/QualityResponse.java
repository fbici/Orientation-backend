package com.orientation.orientationapp.dataplat_import.dto.response;

import com.orientation.orientationapp.dataplat_formats.core.model.QualityReport;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityResponse {

    private UUID importId;
    private QualityReport report;
    private double overallScore;
    private String grade;
}
