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
        private double cpuUsage;
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
        private long totalApiCalls;
        private double averageResponseTime;
        private long errorCount;
        private long activeSessions;
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
        private long responseTimeMs;
    }
}
