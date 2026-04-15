package com.ecommerce.action.consumer;

import com.ecommerce.action.event.AnomalyEvent;
import com.ecommerce.action.service.CooldownGuard;
import com.ecommerce.action.service.SeverityRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionEngineConsumer {

    private final CooldownGuard cooldownGuard;
    private final SeverityRouter severityRouter;

    @KafkaListener(
            topics = "anomaly-events",
            groupId = "action-engine-group",
            containerFactory = "actionKafkaListenerContainerFactory"
    )
    public void onAnomalyEvent(
            @Payload AnomalyEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("[ACTION-ENGINE] Received anomaly | service={} type={} severity={} confidence={} partition={} offset={}",
                event.getServiceId(), event.getAnomalyType(),
                event.getSeverity(), event.getConfidence(),
                partition, offset);

        if (cooldownGuard.isOnCooldown(event.getServiceId(), event.getAnomalyType())) {
            log.warn("[ACTION-ENGINE] Cooldown active — skipping action | service={} type={}",
                    event.getServiceId(), event.getAnomalyType());
            return;
        }

        cooldownGuard.setCooldown(event.getServiceId(), event.getAnomalyType());
        severityRouter.route(event);
    }
}
