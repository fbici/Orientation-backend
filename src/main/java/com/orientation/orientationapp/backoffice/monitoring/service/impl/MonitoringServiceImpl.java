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
@lombok.RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {
    private final javax.sql.DataSource dataSource;

    private static Double processCpu() {
        if(ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean bean) {
            double load=bean.getProcessCpuLoad();return load>=0?load*100:null;
        }
        return null;
    }
    @Override
    public MonitoringResponse getMonitoringData() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        MonitoringResponse.SystemMetrics systemMetrics = MonitoringResponse.SystemMetrics.builder()
                .cpuUsage(processCpu())
                .memoryUsage((double) memoryBean.getHeapMemoryUsage().getUsed() / memoryBean.getHeapMemoryUsage().getMax() * 100)
                .heapUsed(memoryBean.getHeapMemoryUsage().getUsed())
                .heapMax(memoryBean.getHeapMemoryUsage().getMax())
                .nonHeapUsed(memoryBean.getNonHeapMemoryUsage().getUsed())
                .activeThreads(threadBean.getThreadCount())
                .daemonThreads(threadBean.getDaemonThreadCount())
                .uptimeMs(ManagementFactory.getRuntimeMXBean().getUptime())
                .build();

        MonitoringResponse.ApplicationMetrics appMetrics = MonitoringResponse.ApplicationMetrics.builder()
                .totalApiCalls(null)
                .averageResponseTime(null)
                .errorCount(null)
                .activeSessions(null)
                .endpointCalls(Map.of())
                .build();

        boolean databaseUp=false;
        try(var connection=dataSource.getConnection()) { databaseUp=connection.isValid(2); }
        catch(java.sql.SQLException ex) { log.warn("Database health check unavailable"); }
        List<MonitoringResponse.HealthCheck> healthChecks=List.of(
            MonitoringResponse.HealthCheck.builder().name("Database").status(databaseUp?"UP":"DOWN")
                .message(databaseUp?"Connection validated":"Connection unavailable").build(),
            MonitoringResponse.HealthCheck.builder().name("Application").status("UP").message("Running").build());

        return MonitoringResponse.builder()
                .systemMetrics(systemMetrics)
                .applicationMetrics(appMetrics)
                .healthChecks(healthChecks)
                .build();
    }
}
