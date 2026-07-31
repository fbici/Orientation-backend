package com.orientation.orientationapp.intelligence.learning;

import com.orientation.orientationapp.modules.recommendation.entity.Recommendation;
import com.orientation.orientationapp.knowledge.entity.RecommendationFeedback;
import com.orientation.orientationapp.knowledge.repository.RecommendationFeedbackRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Moteur d'apprentissage par feedback.
 *
 * Quand un candidat accepte ou refuse une recommandation,
 * le système mémorise et ajuste progressivement ses scores.
 *
 * Acceptée → le profil du programme est renforcé pour ce type de candidat
 * Refusée → le profil est affaibli
 *
 * Cela permet d'améliorer les recommandations au fil du temps
 * sans dépendre d'un LLM externe.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningEngine {

    private final RecommendationFeedbackRepository feedbackRepository;
    private final SimilarityMatrixService similarityService;

    /**
     * Enregistre un feedback positif (acceptation).
     */
    @Transactional
    public void recordAcceptance(UUID recommendationId, UUID candidateId, UUID programId) {
        log.info("Learning: ACCEPTED recommendation={} candidate={} program={}", recommendationId, candidateId, programId);

        RecommendationFeedback feedback = new RecommendationFeedback();
        feedback.setRecommendationId(recommendationId);
        feedback.setCandidateId(candidateId);
        feedback.setProgramId(programId);
        feedback.setAction("ACCEPTED");
        feedbackRepository.save(feedback);

        // Mettre à jour la matrice de similarité
        similarityService.reinforce(candidateId, programId, 1.0);
    }

    /**
     * Enregistre un feedback négatif (refus).
     */
    @Transactional
    public void recordRejection(UUID recommendationId, UUID candidateId, UUID programId, String reason) {
        log.info("Learning: REJECTED recommendation={} candidate={} program={} reason={}",
                recommendationId, candidateId, programId, reason);

        RecommendationFeedback feedback = new RecommendationFeedback();
        feedback.setRecommendationId(recommendationId);
        feedback.setCandidateId(candidateId);
        feedback.setProgramId(programId);
        feedback.setAction("REJECTED");
        feedback.setReason(reason);
        feedbackRepository.save(feedback);

        // Affaiblir le lien
        similarityService.reinforce(candidateId, programId, -0.5);
    }

    /**
     * Enregistre un feedback de consultation.
     */
    @Transactional
    public void recordView(UUID recommendationId, UUID candidateId, UUID programId) {
        RecommendationFeedback feedback = new RecommendationFeedback();
        feedback.setRecommendationId(recommendationId);
        feedback.setCandidateId(candidateId);
        feedback.setProgramId(programId);
        feedback.setAction("VIEWED");
        feedbackRepository.save(feedback);
    }

    /**
     * Récupère l'historique d'apprentissage pour un candidat.
     */
    public List<RecommendationFeedback> getHistory(UUID candidateId) {
        return feedbackRepository.findByCandidateId(candidateId);
    }

    /**
     * Calcule le score d'ajustement basé sur le feedback historique.
     *
     * Retourne un facteur multiplicatif (0.5 à 1.5) à appliquer
     * au score de recommandation d'un programme pour un type de candidat.
     */
    public double getAdjustmentFactor(UUID candidateId, UUID programId) {
        List<RecommendationFeedback> feedbacks = feedbackRepository.findByCandidateIdAndProgramId(candidateId, programId);

        if (feedbacks.isEmpty()) return 1.0;

        double positive = feedbacks.stream().filter(f -> "ACCEPTED".equals(f.getAction())).count();
        double negative = feedbacks.stream().filter(f -> "REJECTED".equals(f.getAction())).count();

        // Ajustement : +10% par acceptation, -5% par refus, borné entre 0.5 et 1.5
        double factor = 1.0 + (positive * 0.1) - (negative * 0.05);
        return Math.max(0.5, Math.min(1.5, factor));
    }

    /**
     * Retourne les programmes les plus acceptés par des profils similaires.
     */
    public List<UUID> getPopularProgramsForProfile(UUID candidateId) {
        return similarityService.findSimilarPrograms(candidateId);
    }
}
