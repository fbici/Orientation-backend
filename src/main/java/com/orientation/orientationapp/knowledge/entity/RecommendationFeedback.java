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
@Table(name = "knowledge_recommendation_feedback")
public class RecommendationFeedback extends BaseEntity {

    @Column(nullable = false)
    private UUID recommendationId;

    @Column(nullable = false)
    private UUID candidateId;

    @Column(nullable = false)
    private UUID programId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackType feedbackType;

    @Column(precision = 5, scale = 2)
    private BigDecimal rating;

    @Column(length = 500)
    private String comment;

    @Column(nullable = false)
    @Builder.Default
    private Boolean helpful = true;

    public enum FeedbackType {
        ACCEPTED, REJECTED, RATED, COMMENTED
    }
}
