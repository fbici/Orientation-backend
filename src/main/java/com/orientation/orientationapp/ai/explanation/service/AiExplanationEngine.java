package com.orientation.orientationapp.ai.explanation.service;

import java.util.List;

import com.orientation.orientationapp.ai.explanation.model.AiExplanation;

public interface AiExplanationEngine {

    /**
     * Generate AI-powered explanation for a recommendation.
     *
     * @param programName    the program name
     * @param universityName the university name
     * @param score          the match score
     * @param profileSummary profile summary
     * @param criteriaMet    criteria that were met
     * @param criteriaFailed criteria that were not met
     * @return the AI explanation
     */
    AiExplanation generateExplanation(String programName, String universityName,
                                       double score, String profileSummary,
                                       List<String> criteriaMet, List<String> criteriaFailed);

    /**
     * Generate comparison explanation between two programs.
     *
     * @param programA first program name
     * @param programB second program name
     * @param scoreA   score of first program
     * @param scoreB   score of second program
     * @return comparison explanation
     */
    AiExplanation generateComparison(String programA, String programB,
                                      double scoreA, double scoreB);
}
