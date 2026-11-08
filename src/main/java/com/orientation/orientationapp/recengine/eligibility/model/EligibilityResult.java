package com.orientation.orientationapp.recengine.eligibility.model;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EligibilityResult {
    private EligibilityStatus status;
    /** Compatibility field; only ELIGIBLE yields true. */
    private boolean eligible;
    private boolean conditionallyEligible;
    /** Fraction of satisfied criteria, not admission confidence. */
    private BigDecimal eligibilityScore;
    private List<EligibilityCriterion> criteria;
    private List<String> warnings;
    private List<String> blockingIssues;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EligibilityCriterion {
        private UUID criterionId;
        private UUID sourceFactId;
        private UUID sourceDocumentId;
        private UUID sourceDocumentVersionId;
        private Integer sourcePage;
        private String sourcePassage;
        private UUID guideVersionId;
        private UUID academicYearId;
        private String logicalGroup;
        private String logicalOperator;
        private Boolean mandatory;
        private String criterionName;
        private String expectedValue;
        private String actualValue;
        private java.util.Map<String,Object> normalization;
        private EligibilityStatus status;
        private boolean met;
        private String message;
    }
}
