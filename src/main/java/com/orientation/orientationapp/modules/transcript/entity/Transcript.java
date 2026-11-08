package com.orientation.orientationapp.modules.transcript.entity;

import com.orientation.orientationapp.common.enums.TranscriptStatus;
import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.orientation.entity.AcademicYear;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transcripts")
public class Transcript extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transcript_candidate"))
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transcript_academic_year"))
    @com.fasterxml.jackson.annotation.JsonIgnore
    private AcademicYear academicYear;

    @Column(length = 200)
    private String title;

    @Column(length = 200)
    private String institution;

    @Column(precision = 5, scale = 2)
    private BigDecimal average;

    private Integer totalSubjects;

    private Integer totalCredits;

    @Column(length = 50)
    private String mention;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TranscriptStatus status = TranscriptStatus.DRAFT;

    @Column(length = 50)
    private String source;

    @Column(length = 255)
    private String originalFileName;

    @Column(length = 500)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String fileUrl;

    @Column(precision = 5, scale = 2)
    private BigDecimal ocrConfidence;

    private Instant validatedAt;

    private Instant rejectedAt;

    @Column(length = 500)
    private String rejectionReason;

    @OneToMany(mappedBy = "transcript", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TranscriptLine> lines = new HashSet<>();

    @OneToMany(mappedBy = "transcript", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Builder.Default
    private java.util.List<TranscriptAnalysis> analyses = new java.util.ArrayList<>();
}
