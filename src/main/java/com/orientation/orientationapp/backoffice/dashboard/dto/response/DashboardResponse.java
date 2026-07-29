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
        private long totalRecommendations;
        private long totalGuidesImported;
        private long totalDocumentsOcr;
        private long activeRules;
        private long totalTenants;
        private long activeUsers;
        private long todayRecommendations;
        private long activeImports;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentActivity {
        private long recentImports;
        private long recentRecommendations;
        private long recentDocuments;
        private long recentUsers;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemHealth {
        private String status;
        private double cpuUsage;
        private double memoryUsage;
        private long heapUsed;
        private long heapMax;
        private int activeThreads;
        private int databaseConnections;
        private double responseTimeMs;
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
