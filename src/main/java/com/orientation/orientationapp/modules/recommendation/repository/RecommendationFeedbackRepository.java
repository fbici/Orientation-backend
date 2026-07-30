package com.orientation.orientationapp.modules.recommendation.repository;

import com.orientation.orientationapp.modules.recommendation.entity.RecommendationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repository pour les feedbacks de recommandations.
 */
@Repository
public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, UUID> {

    /**
     * Trouve tous les feedbacks d'un candidat.
     */
    List<RecommendationFeedback> findByCandidateId(UUID candidateId);

    /**
     * Trouve les feedbacks d'un candidat pour un programme donné.
     */
    List<RecommendationFeedback> findByCandidateIdAndProgramId(UUID candidateId, UUID programId);

    /**
     * Trouve les feedbacks par action.
     */
    List<RecommendationFeedback> findByAction(String action);

    /**
     * Compte les acceptations pour un programme.
     */
    long countByProgramIdAndAction(UUID programId, String action);

    /**
     * Compte les refus pour un programme.
     */
    long countByProgramIdAndActionAndReasonIsNotNull(UUID programId, String action);
}
