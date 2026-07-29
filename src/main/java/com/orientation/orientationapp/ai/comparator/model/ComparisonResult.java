package com.orientation.orientationapp.ai.comparator.model;

import lombok.*; import java.util.Map;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparisonResult {

    private ComparisonItem itemA;
    private ComparisonItem itemB;
    private List<ComparisonFeature> features;
    private String summary;
    private String recommendation;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparisonItem {
        private String name;
        private String type;
        private double score;
        private Map<String, String> attributes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparisonFeature {
        private String feature;
        private String valueA;
        private String valueB;
        private String winner;
        private String explanation;
    }
}
