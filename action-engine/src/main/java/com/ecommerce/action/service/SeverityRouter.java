package com.ecommerce.action.service;

import com.ecommerce.action.action.LogAndMonitorAction;
import com.ecommerce.action.action.RestartAction;
import com.ecommerce.action.action.ScaleUpAction;
import com.ecommerce.action.event.AnomalyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeverityRouter {

    private final RestartAction restartAction;
    private final ScaleUpAction scaleUpAction;
    private final LogAndMonitorAction logAndMonitorAction;

    public void route(AnomalyEvent event) {
        String severity = event.getSeverity();

        if (severity == null) {
            log.warn("[ROUTER] Null severity — defaulting to LOW | service={}", event.getServiceId());
            logAndMonitorAction.execute(event);
            return;
        }

        switch (severity.toUpperCase()) {
            case "CRITICAL" -> {
                log.warn("[ROUTER] CRITICAL → RestartAction | service={}", event.getServiceId());
                restartAction.execute(event);
            }
            case "WARNING" -> {
                log.warn("[ROUTER] WARNING → ScaleUpAction | service={}", event.getServiceId());
                scaleUpAction.execute(event);
            }
            case "LOW" -> {
                log.info("[ROUTER] LOW → LogAndMonitorAction | service={}", event.getServiceId());
                logAndMonitorAction.execute(event);
            }
            default -> {
                log.warn("[ROUTER] Unknown severity '{}' — falling back to LogAndMonitor", severity);
                logAndMonitorAction.execute(event);
            }
        }
    }
}
