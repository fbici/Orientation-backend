package com.orientation.orientationapp.knowledge.explanation.service;
import java.util.UUID;

import java.util.List;
import java.util.Map;

public interface ExplainableAiServiceV2 {

    /**
     * Generate full explanation for a recommendation.
     *
     * @param recommendationId the recommendation ID
     * @return complete explanation
     */
    Map<String, Object> explain(UUID recommendationId);

    /**
     * Explain why a program was recommended.
     *
     * @param candidateId the candidate ID
     * @param programId   the program ID
     * @return explanation
     */
    Map<String, Object> explainWhyRecommended(UUID candidateId, UUID programId);

    /**
     * Explain why other programs were NOT recommended.
     *
     * @param candidateId the candidate ID
     * @param programId   the program ID
     * @return explanation of exclusions
     */
    Map<String, Object> explainWhyNotRecommended(UUID candidateId, UUID programId);
}
