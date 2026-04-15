package com.ecommerce.action.action;

import com.ecommerce.action.event.AnomalyEvent;
import com.ecommerce.action.service.ActionDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScaleUpAction {

    private final ActionDispatcher dispatcher;

    public void execute(AnomalyEvent event) {
        String serviceId = event.getServiceId();

        // ── STUB MODE ──────────────────────────────────────────────────────────
        // To activate real scale-up, replace this block with:
        //   Kubernetes:   kubectl scale deployment/{serviceId} --replicas=3
        //   Docker Compose: docker compose up --scale {serviceId}=3
        // ─────────────────────────────────────────────────────────────────────
        log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.warn("[SCALE-UP STUB] Would scale up service       : {}", serviceId);
        log.warn("[SCALE-UP STUB] anomalyType={} confidence={}",
                event.getAnomalyType(), event.getConfidence());
        log.warn("[SCALE-UP STUB] Wire Kubernetes/Compose API to activate.");
        log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        dispatcher.recordAction(event, "SCALE_UP", "STUB_EXECUTED");
    }
}
