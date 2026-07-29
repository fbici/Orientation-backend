package com.orientation.orientationapp.recengine.recommendation.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateRecommendationRequest {
    private UUID candidateId;
    private String bacType;
    private BigDecimal bacAverage;
    private Map<String, BigDecimal> subjectGrades;
    private List<String> preferredFields;
    private List<String> preferredCountries;
    private String language;
    private BigDecimal budget;
}
