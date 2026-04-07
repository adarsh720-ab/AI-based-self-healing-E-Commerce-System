package com.ecommerce.ai.consumer;


import com.ecommerce.ai.service.IncidentService;
import com.ecommerce.commons.event.AnomalyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnomalyEventConsumer {

    private final IncidentService incidentService;

    @KafkaListener(
            topics = "anomaly-events",
            groupId = "ai-service-group",
            containerFactory = "anomalyKafkaListenerContainerFactory"
    )
    public void consume(AnomalyEvent event) {
        log.info("Received anomaly event — service={} type={} trace={}",
                event.getServiceId(), event.getAnomalyType(), event.getTraceId());
        try {
            incidentService.processAnomaly(event);
        } catch (Exception e) {
            log.error("Failed to process anomaly event for trace={}: {}",
                    event.getTraceId(), e.getMessage());
        }
    }
}