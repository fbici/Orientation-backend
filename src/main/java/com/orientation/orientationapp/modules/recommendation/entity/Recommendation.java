package com.orientation.orientationapp.modules.recommendation.entity;

import com.orientation.orientationapp.common.enums.RecommendationStatus;
import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.orientation.entity.GuideVersion;
import com.orientation.orientationapp.modules.transcript.entity.Transcript;
import com.orientation.orientationapp.modules.university.entity.Program;
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
@Table(name = "recommendations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_recommendation_run_program", columnNames = {"run_id", "program_id"})
})
public class Recommendation extends BaseEntity {
    private java.util.UUID runId;
    private Long candidateVersion;
    @Enumerated(EnumType.STRING) @Column(length=30)
    private com.orientation.orientationapp.recengine.eligibility.model.EligibilityStatus eligibilityStatus;
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition="jsonb") private String resultPayload;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recommendation_candidate"))
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_version_id", foreignKey = @ForeignKey(name = "fk_recommendation_guide_version"))
    private GuideVersion guideVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recommendation_program"))
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", foreignKey = @ForeignKey(name = "fk_recommendation_transcript"))
    private Transcript transcript;

    @Column(precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(nullable = false)
    private Integer rank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Boolean eligible = false;

    @Column(precision = 5, scale = 2)
    private BigDecimal admissionProbability;

    @Column(nullable = false)
    private Instant recommendedAt;

    private Instant expiresAt;

    @OneToMany(mappedBy = "recommendation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RecommendationExplanation> explanations = new HashSet<>();
}
