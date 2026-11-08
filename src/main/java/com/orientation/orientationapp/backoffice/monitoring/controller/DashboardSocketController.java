package com.orientation.orientationapp.backoffice.monitoring.controller;

import com.orientation.orientationapp.backoffice.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="app.realtime.scheduled-broadcasts",havingValue="true",matchIfMissing=true)
@Controller
@RequiredArgsConstructor
public class DashboardSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DashboardService dashboardService;

    @Scheduled(fixedRate = 30000)
    public void broadcastDashboardUpdate() {
        try {
            // Invalidate the authorized client's HTTP view; no tenant data is broadcast.
            messagingTemplate.convertAndSend("/topic/dashboard", java.util.Map.of(
                "type","KPI_UPDATE","timestamp",java.time.Instant.now().toString()));
        } catch (Exception e) {
            log.error("Failed to broadcast dashboard update", e);
        }
    }
}
