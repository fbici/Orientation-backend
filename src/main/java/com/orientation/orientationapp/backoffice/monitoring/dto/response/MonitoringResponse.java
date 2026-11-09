package com.orientation.orientationapp.backoffice.monitoring.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoringResponse {

    private SystemMetrics systemMetrics;
    private ApplicationMetrics applicationMetrics;
    private List<HealthCheck> healthChecks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemMetrics {
        private Double cpuUsage;
        private double memoryUsage;
        private long heapUsed;
        private long heapMax;
        private long nonHeapUsed;
        private int activeThreads;
        private int daemonThreads;
        private long uptimeMs;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplicationMetrics {
        private Long totalApiCalls;
        private Double averageResponseTime;
        private Long errorCount;
        private Long activeSessions;
        private Map<String, Long> endpointCalls;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HealthCheck {
        private String name;
        private String status;
        private String message;
        private Long responseTimeMs;
    }
}
