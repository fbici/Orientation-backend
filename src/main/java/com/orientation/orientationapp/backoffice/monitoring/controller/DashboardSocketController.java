package com.orientation.orientationapp.backoffice.monitoring.controller;

import com.orientation.orientationapp.backoffice.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DashboardService dashboardService;

    @Scheduled(fixedRate = 30000)
    public void broadcastDashboardUpdate() {
        try {
            var dashboard = dashboardService.getExecutiveDashboard();
            messagingTemplate.convertAndSend("/topic/dashboard", dashboard);
        } catch (Exception e) {
            log.error("Failed to broadcast dashboard update", e);
        }
    }
}
