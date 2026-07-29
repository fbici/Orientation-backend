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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transcript_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_transcript_analysis_transcript"))
    private Transcript transcript;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal normalizedAverage;

    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentile;

    private Integer nationalRank;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalScore;

    @Column(columnDefinition = "jsonb")
    private String strongestSubjects;

    @Column(columnDefinition = "jsonb")
    private String weakestSubjects;

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
