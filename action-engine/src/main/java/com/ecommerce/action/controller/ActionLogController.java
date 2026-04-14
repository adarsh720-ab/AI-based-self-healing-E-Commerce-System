package com.ecommerce.action.controller;

import com.ecommerce.action.model.ActionLog;
import com.ecommerce.action.repository.ActionLogRepository;
import com.ecommerce.action.service.CooldownGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionLogController {

    private final ActionLogRepository actionLogRepository;
    private final CooldownGuard cooldownGuard;

    /**
     * GET /api/actions
     * Returns all action logs, most recent first.
     */
    @GetMapping
    public ResponseEntity<List<ActionLog>> getAllLogs() {
        List<ActionLog> logs = actionLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/actions/service/{serviceId}
     * Returns action history for a specific service.
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ActionLog>> getLogsByService(@PathVariable String serviceId) {
        List<ActionLog> logs = actionLogRepository.findByServiceIdOrderByExecutedAtDesc(serviceId);
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/actions/stats
     * Returns count of each action type in the last 24 hours.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        long restarts  = actionLogRepository.findByActionTypeAndExecutedAtAfter("RESTART", since).size();
        long scaleUps  = actionLogRepository.findByActionTypeAndExecutedAtAfter("SCALE_UP", since).size();
        long monitored = actionLogRepository.findByActionTypeAndExecutedAtAfter("LOG_AND_MONITOR", since).size();

        return ResponseEntity.ok(Map.of(
                "last24h", Map.of(
                        "RESTART", restarts,
                        "SCALE_UP", scaleUps,
                        "LOG_AND_MONITOR", monitored,
                        "total", restarts + scaleUps + monitored
                )
        ));
    }

    /**
     * DELETE /api/actions/cooldown/{serviceId}/{anomalyType}
     * Manually clears cooldown for a service — useful in testing.
     */
    @DeleteMapping("/cooldown/{serviceId}/{anomalyType}")
    public ResponseEntity<Map<String, String>> clearCooldown(
            @PathVariable String serviceId,
            @PathVariable String anomalyType
    ) {
        cooldownGuard.clearCooldown(serviceId, anomalyType);
        return ResponseEntity.ok(Map.of(
                "message", "Cooldown cleared",
                "serviceId", serviceId,
                "anomalyType", anomalyType
        ));
    }
}
