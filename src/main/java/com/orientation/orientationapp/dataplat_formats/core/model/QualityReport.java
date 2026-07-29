package com.orientation.orientationapp.dataplat_formats.core.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityReport {

    private double overallScore;
    private double completenessScore;
    private double accuracyScore;
    private double consistencyScore;
    private double uniquenessScore;
    private int totalRecords;
    private int validRecords;
    private int invalidRecords;
    private int duplicateRecords;
    private int missingValuesCount;
    private Map<String, Integer> missingValuesByField;
    private Map<String, Integer> errorsByField;
    private Map<String, Integer> warningsByField;
    private int unknownColumns;
    private int brokenReferences;

    public static QualityReport empty() {
        return QualityReport.builder()
                .overallScore(0.0)
                .totalRecords(0)
                .build();
    }
}
