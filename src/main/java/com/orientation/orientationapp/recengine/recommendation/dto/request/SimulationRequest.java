package com.orientation.orientationapp.recengine.recommendation.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationRequest {
    private UUID candidateId;
    private Map<String, BigDecimal> modifiedGrades;
    private String modifiedBacType;
    private BigDecimal modifiedBacAverage;
    private String modifiedCountry;
    private BigDecimal modifiedBudget;
    private String modifiedLanguage;
    private String description;
}
