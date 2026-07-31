package com.orientation.orientationapp.intelligence.learning;

import com.orientation.orientationapp.knowledge.entity.RecommendationFeedback;
import com.orientation.orientationapp.knowledge.repository.RecommendationFeedbackRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningEngine {

    private final RecommendationFeedbackRepository feedbackRepository;
    private final SimilarityMatrixService similarityService;

    @Transactional
    public void recordAcceptance(UUID recommendationId, UUID candidateId, UUID programId) {
        log.info("Learning: ACCEPTED reco={} candidate={} program={}", recommendationId, candidateId, programId);
        RecommendationFeedback fb = new RecommendationFeedback();
        fb.setRecommendationId(recommendationId);
        fb.setCandidateId(candidateId);
        fb.setProgramId(programId);
        fb.setFeedbackType(RecommendationFeedback.FeedbackType.ACCEPTED);
        fb.setRating(BigDecimal.valueOf(5));
        fb.setHelpful(true);
        feedbackRepository.save(fb);
        similarityService.reinforce(candidateId, programId, 1.0);
    }

    @Transactional
    public void recordRejection(UUID recommendationId, UUID candidateId, UUID programId, String reason) {
        log.info("Learning: REJECTED reco={} candidate={} program={} reason={}", recommendationId, candidateId, programId, reason);
        RecommendationFeedback fb = new RecommendationFeedback();
        fb.setRecommendationId(recommendationId);
        fb.setCandidateId(candidateId);
        fb.setProgramId(programId);
        fb.setFeedbackType(RecommendationFeedback.FeedbackType.REJECTED);
        fb.setRating(BigDecimal.valueOf(1));
        fb.setComment(reason);
        fb.setHelpful(false);
        feedbackRepository.save(fb);
        similarityService.reinforce(candidateId, programId, -0.5);
    }

    @Transactional
    public void recordView(UUID recommendationId, UUID candidateId, UUID programId) {
        RecommendationFeedback fb = new RecommendationFeedback();
        fb.setRecommendationId(recommendationId);
        fb.setCandidateId(candidateId);
        fb.setProgramId(programId);
        fb.setFeedbackType(RecommendationFeedback.FeedbackType.COMMENTED);
        fb.setRating(BigDecimal.valueOf(3));
        fb.setHelpful(true);
        feedbackRepository.save(fb);
    }

    public List<RecommendationFeedback> getHistory(UUID candidateId) {
        return feedbackRepository.findByCandidateId(candidateId);
    }

    public double getAdjustmentFactor(UUID candidateId, UUID programId) {
        List<RecommendationFeedback> fbs = feedbackRepository.findByCandidateId(candidateId);
        if (fbs.isEmpty()) return 1.0;
        double positive = fbs.stream().filter(f -> f.getFeedbackType() == RecommendationFeedback.FeedbackType.ACCEPTED).count();
        double negative = fbs.stream().filter(f -> f.getFeedbackType() == RecommendationFeedback.FeedbackType.REJECTED).count();
        double factor = 1.0 + (positive * 0.1) - (negative * 0.05);
        return Math.max(0.5, Math.min(1.5, factor));
    }

    public List<UUID> getPopularProgramsForProfile(UUID candidateId) {
        return similarityService.findSimilarPrograms(candidateId);
    }
}
