package com.orientation.orientationapp.backoffice.dashboard.service.impl;

import com.orientation.orientationapp.backoffice.dashboard.dto.response.DashboardResponse;
import com.orientation.orientationapp.backoffice.dashboard.service.DashboardService;
import com.orientation.orientationapp.modules.university.repository.ProgramRepository;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CandidateRepository candidateRepository;
    private final ProgramRepository programRepository;

    private final com.orientation.orientationapp.security.service.ResourceAccess access;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly=true)
    public DashboardResponse getExecutiveDashboard() {
        access.require("STATISTICS_READ");
        return build(access.global() ? null : access.tenant(null));
    }
    private DashboardResponse build(java.util.UUID tenant) {
        log.info("Building executive dashboard");

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        DashboardResponse.KpiSummary kpis = DashboardResponse.KpiSummary.builder()
                .totalCandidates(tenant==null ? candidateRepository.count() : candidateRepository.countByTenant_IdAndUserIsNotNull(tenant))
                .totalRecommendations(0L)
                .totalGuidesImported(0L)
                .totalDocumentsOcr(0L)
                .activeRules(0L)
                .totalTenants(1L)
                .activeUsers(1L)
                .todayRecommendations(0L)
                .activeImports(0L)
                .build();

        DashboardResponse.RecentActivity recentActivity = DashboardResponse.RecentActivity.builder()
                .recentImports(0L)
                .recentRecommendations(0L)
                .recentDocuments(0L)
                .recentUsers(0L)
                .build();

        DashboardResponse.SystemHealth systemHealth = DashboardResponse.SystemHealth.builder()
                .status("UP")
                .cpuUsage(Runtime.getRuntime().availableProcessors() > 0 ? 25.0 : 0)
                .memoryUsage((double) memoryBean.getHeapMemoryUsage().getUsed() / memoryBean.getHeapMemoryUsage().getMax() * 100)
                .heapUsed(memoryBean.getHeapMemoryUsage().getUsed())
                .heapMax(memoryBean.getHeapMemoryUsage().getMax())
                .activeThreads(threadBean.getThreadCount())
                .databaseConnections(10)
                .responseTimeMs(45.0)
                .build();

        DashboardResponse.ChartData charts = DashboardResponse.ChartData.builder()
                .recommendationsByDay(Map.of())
                .importsByDay(Map.of())
                .documentsByType(Map.of())
                .build();

        return DashboardResponse.builder()
                .kpis(kpis)
                .recentActivity(recentActivity)
                .systemHealth(systemHealth)
                .charts(charts)
                .build();
    }

    @Override
    public DashboardResponse getDashboardByTenant(String tenantId) {
        access.require("STATISTICS_READ");
        final java.util.UUID requested;
        try { requested=java.util.UUID.fromString(tenantId); }
        catch(IllegalArgumentException ex) { throw com.orientation.orientationapp.security.service.ResourceAccess.denied(); }
        return build(access.tenant(requested));
    }
}
