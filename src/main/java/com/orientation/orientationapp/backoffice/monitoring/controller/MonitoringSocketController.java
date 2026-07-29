package com.orientation.orientationapp.backoffice.monitoring.controller;

import com.orientation.orientationapp.backoffice.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MonitoringSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MonitoringService monitoringService;

    @Scheduled(fixedRate = 15000)
    public void broadcastMonitoringUpdate() {
        try {
            var monitoring = monitoringService.getMonitoringData();
            messagingTemplate.convertAndSend("/topic/monitoring", monitoring);
        } catch (Exception e) {
            log.error("Failed to broadcast monitoring update", e);
        }
    }
}
