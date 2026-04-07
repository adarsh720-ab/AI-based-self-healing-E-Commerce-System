package com.ecommerce.ai.service;

import com.ecommerce.ai.model.Incident;
import com.ecommerce.ai.repository.IncidentRepository;
import com.ecommerce.commons.event.AnomalyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final RootCauseAnalyser  rootCauseAnalyser;

    public void processAnomaly(AnomalyEvent event) {
        log.info("Processing anomaly for service={} type={} confidence={}",
                event.getServiceId(), event.getAnomalyType(), event.getConfidence());

        // Call LLM for root cause analysis
        RootCauseAnalyser.AnalysisResult result = rootCauseAnalyser.analyse(event);

        // Build and save incident
        Incident incident = Incident.builder()
                .serviceId(event.getServiceId())
                .anomalyType(event.getAnomalyType())
                .anomalyScore(event.getAnomalyScore())
                .confidence(event.getConfidence())
                .errorCode(event.getErrorCode())
                .message(event.getMessage())
                .rootCause(result.rootCause())
                .suggestedFix(result.suggestedFix())
                .severity(result.severity())
                .traceId(event.getTraceId())
                .detectedAt(event.getDetectedAt())
                .analysedAt(LocalDateTime.now())
                .status(Incident.IncidentStatus.OPEN)
                .build();

        Incident saved = incidentRepository.save(incident);

        log.warn("INCIDENT SAVED | id={} | service={} | severity={} | rootCause={}",
                saved.getId(),
                saved.getServiceId(),
                saved.getSeverity(),
                saved.getRootCause());
    }
}