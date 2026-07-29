package com.orientation.orientationapp.recengine.eligibility.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityResult {

    private boolean eligible;
    private boolean conditionallyEligible;
    private BigDecimal eligibilityScore;
    private List<EligibilityCriterion> criteria;
    private List<String> warnings;
    private List<String> blockingIssues;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EligibilityCriterion {
        private String criterionName;
        private String expectedValue;
        private String actualValue;
        private boolean met;
        private String message;
    }
}
