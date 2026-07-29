package com.orientation.orientationapp.dataplat_quality.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quality_report_records")
public class QualityReportRecord extends BaseEntity {

    @Column(nullable = false)
    private UUID importHistoryId;

    @Column(nullable = false)
    private Integer totalRows;

    @Column(nullable = false)
    @Builder.Default
    private Integer validRows = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer invalidRows = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer duplicateRows = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer missingValuesCount = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal validationScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal transformationScore;

    @Column(columnDefinition = "jsonb")
    private String warningsJson;

    @Column(columnDefinition = "jsonb")
    private String errorsJson;

    @Column(columnDefinition = "jsonb")
    private String missingValuesJson;
}
