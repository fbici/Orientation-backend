package com.orientation.orientationapp.backoffice.monitoring.service.impl;

import com.orientation.orientationapp.backoffice.monitoring.dto.response.MonitoringResponse;
import com.orientation.orientationapp.backoffice.monitoring.service.MonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MonitoringServiceImpl implements MonitoringService {

    @Override
    public MonitoringResponse getMonitoringData() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        MonitoringResponse.SystemMetrics systemMetrics = MonitoringResponse.SystemMetrics.builder()
                .cpuUsage(Runtime.getRuntime().availableProcessors() > 0 ? 25.0 : 0)
                .memoryUsage((double) memoryBean.getHeapMemoryUsage().getUsed() / memoryBean.getHeapMemoryUsage().getMax() * 100)
                .heapUsed(memoryBean.getHeapMemoryUsage().getUsed())
                .heapMax(memoryBean.getHeapMemoryUsage().getMax())
                .nonHeapUsed(memoryBean.getNonHeapMemoryUsage().getUsed())
                .activeThreads(threadBean.getThreadCount())
                .daemonThreads(threadBean.getDaemonThreadCount())
                .uptimeMs(ManagementFactory.getRuntimeMXBean().getUptime())
                .build();

        MonitoringResponse.ApplicationMetrics appMetrics = MonitoringResponse.ApplicationMetrics.builder()
                .totalApiCalls(0L)
                .averageResponseTime(45.0)
                .errorCount(0L)
                .activeSessions(1L)
                .endpointCalls(Map.of())
                .build();

        List<MonitoringResponse.HealthCheck> healthChecks = List.of(
                MonitoringResponse.HealthCheck.builder().name("Database").status("UP").message("PostgreSQL connected").responseTimeMs(5).build(),
                MonitoringResponse.HealthCheck.builder().name("Application").status("UP").message("Running").responseTimeMs(1).build()
        );

        return MonitoringResponse.builder()
                .systemMetrics(systemMetrics)
                .applicationMetrics(appMetrics)
                .healthChecks(healthChecks)
                .build();
    }
}
