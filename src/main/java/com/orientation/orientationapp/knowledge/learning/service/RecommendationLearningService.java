package com.orientation.orientationapp.knowledge.learning.service;

import com.orientation.orientationapp.knowledge.entity.RecommendationFeedback;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface RecommendationLearningService {

    /**
     * Record user feedback on a recommendation.
     *
     * @param candidateId the candidate ID
     * @param programId   the program ID
     * @param feedback    the feedback type
     * @param rating      optional rating
     * @param comment     optional comment
     */
    void recordFeedback(UUID candidateId, UUID programId,
                        RecommendationFeedback.FeedbackType feedback,
                        BigDecimal rating, String comment);

    /**
     * Get learning statistics.
     *
     * @return statistics
     */
    Map<String, Object> getLearningStatistics();

    /**
     * Get improvement suggestions based on feedback.
     *
     * @param programId the program ID
     * @return improvement suggestions
     */
    Map<String, Object> getImprovementSuggestions(UUID programId);
}
