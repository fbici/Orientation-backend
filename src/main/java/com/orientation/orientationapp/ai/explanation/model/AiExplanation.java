package com.orientation.orientationapp.ai.explanation.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiExplanation {

    private String headline;
    private String summary;
    private List<ExplanationPoint> strengths;
    private List<ExplanationPoint> criteria;
    private List<ExplanationPoint> warnings;
    private List<ExplanationPoint> alternatives;
    private String personalizedAdvice;
    private double confidenceScore;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExplanationPoint {
        private String category;
        private String text;
        private String detail;
        private boolean positive;
        private String impact;
        private String icon;
    }
}
