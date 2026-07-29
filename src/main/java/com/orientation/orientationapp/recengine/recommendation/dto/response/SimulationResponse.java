package com.orientation.orientationapp.recengine.recommendation.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResponse {

    private List<RecommendationItem> simulatedRecommendations;
    private List<SimulationDifference> differences;
    private String summary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendationItem {
        private Integer rank;
        private String programName;
        private String universityName;
        private BigDecimal score;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimulationDifference {
        private String type;
        private String programName;
        private String description;
        private int rankChange;
    }
}
