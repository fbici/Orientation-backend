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
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

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
                .totalCandidates(count("select count(*) from candidates c where not c.deleted and c.user_id is not null",tenant,"c.tenant_id",false))
                .totalUniversities(count("select count(*) from universities u where not u.deleted and u.active",tenant,"u.tenant_id",true))
                .totalRecommendations(count("select count(*) from recommendations r join candidates c on c.id=r.candidate_id where not r.deleted and not c.deleted and c.user_id is not null",tenant,"c.tenant_id",false))
                .totalGuidesImported(count("select count(*) from orientation_guides g where not g.deleted",tenant,"g.tenant_id",true))
                .totalDocumentsOcr(count("select count(*) from doc_documents d where not d.deleted and d.status in ('READY_REVIEW','VALIDATED','PARTIALLY_PUBLISHED','PUBLISHED')",tenant,"d.tenant_id",false))
                .activeRules(count("select count(*) from admission_criteria a join guide_versions v on v.id=a.guide_version_id join orientation_guides g on g.id=v.orientation_guide_id where not a.deleted and not v.deleted and v.active and not g.deleted",tenant,"g.tenant_id",true))
                .totalTenants(count("select count(*) from tenants t where not t.deleted",tenant,"t.id",false))
                .activeUsers(count("select count(*) from users u where not u.deleted and u.enabled and u.status='ACTIVE'",tenant,"u.tenant_id",false))
                .todayRecommendations(count("select count(*) from recommendations r join candidates c on c.id=r.candidate_id where not r.deleted and not c.deleted and c.user_id is not null and r.created_at>=date_trunc('day',now())",tenant,"c.tenant_id",false))
                .activeImports(count("select count(*) from import_history_records i where not i.deleted and i.status in ('PENDING','PROCESSING')",tenant,"i.tenant_id",false))
                .build();

        DashboardResponse.RecentActivity recentActivity = DashboardResponse.RecentActivity.builder()
                .recentImports(null)
                .recentRecommendations(null)
                .recentDocuments(null)
                .recentUsers(null)
                .build();

        DashboardResponse.SystemHealth systemHealth = DashboardResponse.SystemHealth.builder()
                .status("UP")
                .cpuUsage(null)
                .memoryUsage((double) memoryBean.getHeapMemoryUsage().getUsed() / memoryBean.getHeapMemoryUsage().getMax() * 100)
                .heapUsed(memoryBean.getHeapMemoryUsage().getUsed())
                .heapMax(memoryBean.getHeapMemoryUsage().getMax())
                .activeThreads(threadBean.getThreadCount())
                .databaseConnections(null)
                .responseTimeMs(null)
                .build();

        DashboardResponse.ChartData charts = DashboardResponse.ChartData.builder()
                .recommendationsByDay(series("select to_char(r.created_at at time zone 'UTC','YYYY-MM-DD') as bucket,count(*) as total from recommendations r join candidates c on c.id=r.candidate_id where not r.deleted and not c.deleted and c.user_id is not null and r.created_at>=now()-interval '365 days'",tenant,"c.tenant_id",false))
                .importsByDay(series("select to_char(i.created_at at time zone 'UTC','YYYY-MM-DD') as bucket,count(*) as total from import_history_records i where not i.deleted and i.created_at>=now()-interval '365 days'",tenant,"i.tenant_id",false))
                .documentsByType(series("select d.document_type as bucket,count(*) as total from doc_documents d where not d.deleted",tenant,"d.tenant_id",false))
                .build();

        return DashboardResponse.builder()
                .kpis(kpis)
                .recentActivity(recentActivity)
                .systemHealth(systemHealth)
                .charts(charts)
                .build();
    }

    private String scope(java.util.UUID tenant,String field,boolean shared) {
        return tenant==null?"":" and ("+(shared?field+" is null or ":"")+field+"::text = ?)";
    }
    private Long count(String sql,java.util.UUID tenant,String field,boolean shared) {
        Object[] parameters=tenant==null?new Object[0]:new Object[]{tenant.toString()};
        return jdbc.queryForObject(sql+scope(tenant,field,shared),Long.class,parameters);
    }
    private Map<String,Long> series(String sql,java.util.UUID tenant,String field,boolean shared) {
        Map<String,Long> result=new java.util.LinkedHashMap<>();
        Object[] parameters=tenant==null?new Object[0]:new Object[]{tenant.toString()};
        jdbc.query(sql+scope(tenant,field,shared)+" group by bucket order by bucket",
            (org.springframework.jdbc.core.RowCallbackHandler) row->result.put(row.getString("bucket"),row.getLong("total")),parameters);
        return result;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly=true)
    public DashboardResponse getDashboardByTenant(String tenantId) {
        access.require("STATISTICS_READ");
        final java.util.UUID requested;
        try { requested=java.util.UUID.fromString(tenantId); }
        catch(IllegalArgumentException ex) { throw com.orientation.orientationapp.security.service.ResourceAccess.denied(); }
        return build(access.tenant(requested));
    }
}
