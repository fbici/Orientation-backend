package com.orientation.orientationapp.backoffice.analytics.service.impl;

import com.orientation.orientationapp.backoffice.analytics.dto.response.AnalyticsResponse;
import com.orientation.orientationapp.backoffice.analytics.service.AnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Override
    public AnalyticsResponse getAnalytics() {
        log.info("Building analytics");

        AnalyticsResponse.TopEntities topUniversities = AnalyticsResponse.TopEntities.builder()
                .items(List.of(
                        AnalyticsResponse.EntityStat.builder().name("Université Mohammed V").count(150L).score(BigDecimal.valueOf(85)).build(),
                        AnalyticsResponse.EntityStat.builder().name("Université Hassan II").count(120L).score(BigDecimal.valueOf(82)).build(),
                        AnalyticsResponse.EntityStat.builder().name("Université Cadi Ayyad").count(95L).score(BigDecimal.valueOf(78)).build()
                ))
                .build();

        AnalyticsResponse.TopEntities topPrograms = AnalyticsResponse.TopEntities.builder()
                .items(List.of(
                        AnalyticsResponse.EntityStat.builder().name("Licence Informatique").count(200L).score(BigDecimal.valueOf(90)).build(),
                        AnalyticsResponse.EntityStat.builder().name("Licence Médecine").count(180L).score(BigDecimal.valueOf(88)).build(),
                        AnalyticsResponse.EntityStat.builder().name("Licence Droit").count(150L).score(BigDecimal.valueOf(75)).build()
                ))
                .build();

        AnalyticsResponse.EvolutionData evolution = AnalyticsResponse.EvolutionData.builder()
                .daily(Map.of())
                .monthly(Map.of())
                .yearly(Map.of())
                .build();

        AnalyticsResponse.KpiData kpis = AnalyticsResponse.KpiData.builder()
                .acceptanceRate(BigDecimal.valueOf(72.5))
                .recommendationSuccessRate(BigDecimal.valueOf(85.3))
                .averageScore(BigDecimal.valueOf(72.8))
                .averageConfidence(BigDecimal.valueOf(78.5))
                .averageProcessingTime(BigDecimal.valueOf(1250))
                .ocrAccuracy(BigDecimal.valueOf(92.1))
                .importSuccessRate(BigDecimal.valueOf(95.8))
                .build();

        return AnalyticsResponse.builder()
                .topUniversities(topUniversities)
                .topPrograms(topPrograms)
                .topScholarships(AnalyticsResponse.TopEntities.builder().items(List.of()).build())
                .topCountries(AnalyticsResponse.TopEntities.builder().items(List.of()).build())
                .evolution(evolution)
                .kpis(kpis)
                .build();
    }

    @Override
    public AnalyticsResponse getAnalyticsByTenant(String tenantId) {
        return getAnalytics();
    }
}
