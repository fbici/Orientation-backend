package com.orientation.orientationapp.modules.auth.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
@Component @RequiredArgsConstructor @Slf4j
@ConditionalOnProperty(name="app.account-erasure.enabled",havingValue="true",matchIfMissing=true)
public class CandidateErasureJob {
    private final JdbcTemplate jdbc;
    private final CandidateErasureService erasure;
    @Scheduled(initialDelay=60000,fixedDelay=3600000)
    public void run() {
        for(var id:jdbc.queryForList("SELECT user_id FROM candidate_deletion_requests WHERE purge_after<=now() ORDER BY purge_after LIMIT 25",UUID.class)) {
            try { if(erasure.eraseDue(id))log.info("Candidate erasure completed id={}",id); }
            catch(RuntimeException ex) { log.error("Candidate erasure needs retry/review id={} category={}",id,ex.getClass().getSimpleName()); }
        }
    }
}
