package com.orientation.orientationapp.recengine.simulation.model;

import com.orientation.orientationapp.recengine.ranking.model.RankedRecommendation;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResult {

    private List<RankedRecommendation> originalRecommendations;
    private List<RankedRecommendation> simulatedRecommendations;
    private List<SimulationDifference> differences;
    private String summary;

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
