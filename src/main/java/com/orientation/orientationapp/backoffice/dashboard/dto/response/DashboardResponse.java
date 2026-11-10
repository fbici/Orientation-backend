package com.orientation.orientationapp.backoffice.dashboard.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private KpiSummary kpis;
    private RecentActivity recentActivity;
    private SystemHealth systemHealth;
    private ChartData charts;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KpiSummary {
        private long totalCandidates;
        private Long totalUniversities;
        private Long totalRecommendations;
        private Long totalGuidesImported;
        private Long totalDocumentsOcr;
        private Long activeRules;
        private Long totalTenants;
        private Long activeUsers;
        private Long todayRecommendations;
        private Long activeImports;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentActivity {
        private Long recentImports;
        private Long recentRecommendations;
        private Long recentDocuments;
        private Long recentUsers;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemHealth {
        private String status;
        private Double cpuUsage;
        private double memoryUsage;
        private long heapUsed;
        private long heapMax;
        private int activeThreads;
        private Integer databaseConnections;
        private Double responseTimeMs;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChartData {
        private Map<String, Long> recommendationsByDay;
        private Map<String, Long> importsByDay;
        private Map<String, Long> documentsByType;
    }
}
