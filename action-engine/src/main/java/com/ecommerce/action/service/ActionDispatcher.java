package com.ecommerce.action.service;

import com.ecommerce.action.event.AnomalyEvent;
import com.ecommerce.action.model.ActionLog;
import com.ecommerce.action.repository.ActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionDispatcher {

    private final ActionLogRepository actionLogRepository;

    /**
     * Called by each action after executing. Persists a record of what was done.
     */
    public void recordAction(AnomalyEvent event, String actionType, String outcome) {
        ActionLog record = ActionLog.builder()
                .serviceId(event.getServiceId())
                .anomalyType(event.getAnomalyType())
                .anomalyScore(event.getAnomalyScore())
                .severity(event.getSeverity())
                .confidence(event.getConfidence())
                .actionType(actionType)
                .outcome(outcome)
                .traceId(event.getTraceId())
                .executedAt(LocalDateTime.now())
                .build();

        actionLogRepository.save(record);
        log.info("[DISPATCHER] Action recorded | service={} action={} outcome={}",
                event.getServiceId(), actionType, outcome);
    }
}
