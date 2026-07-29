package com.orientation.orientationapp.backoffice.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {

    private TopEntities topUniversities;
    private TopEntities topPrograms;
    private TopEntities topScholarships;
    private TopEntities topCountries;
    private EvolutionData evolution;
    private KpiData kpis;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopEntities {
        private List<EntityStat> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EntityStat {
        private String name;
        private long count;
        private BigDecimal score;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EvolutionData {
        private Map<String, Long> daily;
        private Map<String, Long> monthly;
        private Map<String, Long> yearly;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KpiData {
        private BigDecimal acceptanceRate;
        private BigDecimal recommendationSuccessRate;
        private BigDecimal averageScore;
        private BigDecimal averageConfidence;
        private BigDecimal averageProcessingTime;
        private BigDecimal ocrAccuracy;
        private BigDecimal importSuccessRate;
    }
}
