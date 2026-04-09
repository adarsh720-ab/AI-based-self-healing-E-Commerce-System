package com.ecommerce.ai.service;

import com.ecommerce.ai.model.Incident;
import com.ecommerce.commons.event.AnomalyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RootCauseAnalyser {

    private final ChatClient chatClient;

    @Value("${spring.ai.prompts.system}")
    private String systemPrompt;

    public AnalysisResult analyse(AnomalyEvent event) {
        String userPrompt = buildPrompt(event);
        log.debug("Sending prompt to LLM for service={} type={}",
                event.getServiceId(), event.getAnomalyType());

        try {
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.debug("LLM raw response: {}", response);
            return parseResponse(response, event);

        } catch (Exception e) {
            log.error("LLM call failed for trace={}: {}", event.getTraceId(), e.getMessage());
            return fallbackResult(event);
        }
    }

    private String buildPrompt(AnomalyEvent event) {
        return String.format("""
                Analyze this anomaly from a Spring Boot microservice:

                SERVICE: %s
                ANOMALY TYPE: %s
                CONFIDENCE: %s
                ANOMALY SCORE: %s
                ERROR CODE: %s
                MESSAGE: %s
                HTTP METHOD: %s
                STATUS CODE: %s
                LATENCY MS: %s
                DETECTED AT: %s

                Respond with ONLY this JSON format, nothing else:
                {
                  "rootCause": "one clear sentence explaining why this failure occurred",
                  "suggestedFix": "numbered steps to fix this issue",
                  "severity": "CRITICAL or WARNING or LOW"
                }

                Severity rules:
                - CRITICAL: service completely down, connection refused, 503 errors
                - WARNING: high latency, intermittent errors, degraded performance
                - LOW: single errors, validation failures, expected business errors
                """,
                event.getServiceId(),
                event.getAnomalyType(),
                event.getConfidence(),
                event.getAnomalyScore(),
                event.getErrorCode(),
                event.getMessage(),
                event.getHttpMethod(),
                event.getStatusCode(),
                event.getLatencyMs(),
                event.getDetectedAt()
        );
    }

    private AnalysisResult parseResponse(String response, AnomalyEvent event) {
        try {
            String cleaned = response
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            int start = cleaned.indexOf("{");
            int end   = cleaned.lastIndexOf("}");
            if (start == -1 || end == -1) {
                log.warn("No JSON found in LLM response, using fallback");
                return fallbackResult(event);
            }

            String json         = cleaned.substring(start, end + 1);
            String rootCause    = extractField(json, "rootCause");
            String suggestedFix = extractField(json, "suggestedFix");
            String severityStr  = extractField(json, "severity");

            Incident.Severity severity = parseSeverity(severityStr, event);

            log.info("LLM analysis complete — service={} severity={} rootCause={}",
                    event.getServiceId(), severity, rootCause);

            return new AnalysisResult(rootCause, suggestedFix, severity);

        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", e.getMessage());
            return fallbackResult(event);
        }
    }

    private String extractField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return "Unknown";

        int colonIndex = json.indexOf(":", keyIndex);
        int valueStart = json.indexOf("\"", colonIndex) + 1;
        int valueEnd   = json.indexOf("\"", valueStart);

        if (valueStart <= 0 || valueEnd <= 0) return "Unknown";
        return json.substring(valueStart, valueEnd);
    }

    private Incident.Severity parseSeverity(String severityStr, AnomalyEvent event) {
        if (severityStr == null) return mapFromConfidence(event);
        return switch (severityStr.toUpperCase().trim()) {
            case "CRITICAL" -> Incident.Severity.CRITICAL;
            case "WARNING"  -> Incident.Severity.WARNING;
            default         -> Incident.Severity.LOW;
        };
    }

    private Incident.Severity mapFromConfidence(AnomalyEvent event) {
        return switch (event.getConfidence() != null ? event.getConfidence() : "LOW") {
            case "HIGH"   -> Incident.Severity.CRITICAL;
            case "MEDIUM" -> Incident.Severity.WARNING;
            default       -> Incident.Severity.LOW;
        };
    }

    private AnalysisResult fallbackResult(AnomalyEvent event) {
        String rootCause = String.format(
                "%s detected in %s — errorCode: %s, message: %s",
                event.getAnomalyType(), event.getServiceId(),
                event.getErrorCode(), event.getMessage());
        String suggestedFix = "1. Check service logs. " +
                "2. Verify service health. " +
                "3. Restart service if down. " +
                "4. Check downstream dependencies.";
        return new AnalysisResult(rootCause, suggestedFix, mapFromConfidence(event));
    }

    public record AnalysisResult(
            String rootCause,
            String suggestedFix,
            Incident.Severity severity
    ) {}
}