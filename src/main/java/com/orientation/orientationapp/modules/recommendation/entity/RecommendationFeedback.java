package com.orientation.orientationapp.modules.recommendation.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

/**
 * Entité Recommendation Feedback — feedback des candidats sur les recommandations.
 *
 * Enregistre les acceptations, refus et consultations
 * pour alimenter le moteur d'apprentissage.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recommendation_feedback")
public class RecommendationFeedback extends BaseEntity {

    @Column(nullable = false)
    private UUID recommendationId;

    @Column(nullable = false)
    private UUID candidateId;

    @Column(nullable = false)
    private UUID programId;

    @Column(nullable = false, length = 20)
    private String action; // ACCEPTED, REJECTED, VIEWED

    @Column(length = 500)
    private String reason;
}
