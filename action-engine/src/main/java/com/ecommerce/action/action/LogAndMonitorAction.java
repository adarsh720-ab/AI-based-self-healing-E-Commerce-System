package com.ecommerce.action.action;

import com.ecommerce.action.event.AnomalyEvent;
import com.ecommerce.action.service.ActionDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogAndMonitorAction {

    private final ActionDispatcher dispatcher;

    public void execute(AnomalyEvent event) {
        log.info("[MONITOR] Low-severity anomaly observed — logging only.");
        log.info("[MONITOR] service={} type={} score={} confidence={} traceId={}",
                event.getServiceId(),
                event.getAnomalyType(),
                event.getAnomalyScore(),
                event.getConfidence(),
                event.getTraceId());

        dispatcher.recordAction(event, "LOG_AND_MONITOR", "LOGGED");
    }
}
