package com.orientation.orientationapp.recengine.recommendation.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {

    private List<RecommendationItem> recommendations;
    private int totalPrograms;
    private String profileSummary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendationItem {
        private Integer rank;
        private String programName;
        private String universityName;
        private String facultyName;
        private BigDecimal score;
        private BigDecimal confidence;
        private BigDecimal admissionProbability;
        private String difficultyLevel;
        private boolean eligible;
        private String explanationSummary;
        private List<String> strengths;
        private List<String> warnings;
    }
}
