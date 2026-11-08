package com.orientation.orientationapp.modules.recommendation.entity;
import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="recommendation_runs") @Getter @Setter @NoArgsConstructor
public class RecommendationRun extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="candidate_id",nullable=false)
    private Candidate candidate;
    @Column(nullable=false) private Long candidateVersion;
    @Column(nullable=false) private Instant computedAt;
    @Column(nullable=false) private Integer totalPrograms;
}
