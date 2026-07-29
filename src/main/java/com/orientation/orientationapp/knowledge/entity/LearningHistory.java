package com.orientation.orientationapp.knowledge.entity;

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
@Table(name = "knowledge_learning_history")
public class LearningHistory extends BaseEntity {

    @Column(nullable = false)
    private UUID candidateId;

    @Column(nullable = false)
    private UUID programId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LearningEvent event;

    @Column(precision = 5, scale = 2)
    private BigDecimal scoreBefore;

    @Column(precision = 5, scale = 2)
    private BigDecimal scoreAfter;

    @Column(columnDefinition = "jsonb")
    private String context;

    public enum LearningEvent {
        RECOMMENDATION_ACCEPTED,
        RECOMMENDATION_REJECTED,
        SCORE_IMPROVED,
        SCORE_DECLINED,
        PATTERN_DETECTED
    }
}
