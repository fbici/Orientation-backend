package com.orientation.orientationapp.knowledge.learning.service.impl;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

import com.orientation.orientationapp.knowledge.entity.RecommendationFeedback;
import com.orientation.orientationapp.knowledge.learning.service.RecommendationLearningService;
import com.orientation.orientationapp.knowledge.repository.RecommendationFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultRecommendationLearningService implements RecommendationLearningService {

    private final RecommendationFeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public void recordFeedback(UUID candidateId, UUID programId,
                                RecommendationFeedback.FeedbackType feedback,
                                BigDecimal rating, String comment) {
        log.info("Recording feedback: candidate={}, program={}, type={}", candidateId, programId, feedback);

        RecommendationFeedback feedbackEntity = RecommendationFeedback.builder()
                .candidateId(candidateId)
                .programId(programId)
                .feedbackType(feedback)
                .rating(rating)
                .comment(comment)
                .helpful(feedback != RecommendationFeedback.FeedbackType.REJECTED)
                .build();

        feedbackRepository.save(feedbackEntity);
        log.info("Feedback recorded successfully");
    }

    @Override
    public Map<String, Object> getLearningStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFeedback", feedbackRepository.count());
        stats.put("accepted", feedbackRepository.countByProgramIdAndFeedbackType(null, RecommendationFeedback.FeedbackType.ACCEPTED));
        stats.put("rejected", feedbackRepository.countByProgramIdAndFeedbackType(null, RecommendationFeedback.FeedbackType.REJECTED));
        return stats;
    }

    @Override
    public Map<String, Object> getImprovementSuggestions(UUID programId) {
        Map<String, Object> suggestions = new HashMap<>();
        long accepted = feedbackRepository.countByProgramIdAndFeedbackType(programId, RecommendationFeedback.FeedbackType.ACCEPTED);
        long rejected = feedbackRepository.countByProgramIdAndFeedbackType(programId, RecommendationFeedback.FeedbackType.REJECTED);

        suggestions.put("acceptanceRate", accepted + rejected > 0 ? (double) accepted / (accepted + rejected) * 100 : 0);
        suggestions.put("totalFeedback", accepted + rejected);

        return suggestions;
    }
}
