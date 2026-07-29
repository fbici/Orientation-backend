package com.orientation.orientationapp.recengine.explanation.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Explanation {

    private String summary;
    private List<ExplanationItem> strengths;
    private List<ExplanationItem> criteria;
    private List<ExplanationItem> warnings;
    private String recommendation;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExplanationItem {
        private String category;
        private String text;
        private boolean positive;
        private String impact;
    }
}
