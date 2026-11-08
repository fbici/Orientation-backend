package com.orientation.orientationapp.modules.transcript.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transcript_analyses")
public class TranscriptAnalysis extends BaseEntity {
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition="jsonb") private String reviewPayload;
    private Instant confirmedAt;
    private java.util.UUID parentAnalysisId;
    @Column(length=64) private String sourceChecksum;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transcript_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transcript_analysis_transcript"))
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Transcript transcript;

    @Column(precision = 5, scale = 2)
    private BigDecimal normalizedAverage;

    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentile;

    private Integer nationalRank;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalScore;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String strongestSubjects;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String weakestSubjects;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String subjectAverages;

    @Column(precision = 5, scale = 2)
    private BigDecimal eligibilityScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal scholarshipScore;

    @Column(nullable = false, length = 20)
    private String analysisVersion;

    @Column(nullable = false)
    private Instant analyzedAt;
}
