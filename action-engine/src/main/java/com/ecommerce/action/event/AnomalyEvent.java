package com.ecommerce.action.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mirrors commons AnomalyEvent. Kept self-contained so the module
 * compiles without the parent commons JAR on the classpath.
 * If you have commons on the classpath, replace usages with the commons class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnomalyEvent {

    private String traceId;
    private String serviceId;
    private String method;
    private String level;
    private String errorCode;
    private String stackTrace;
    private String message;
    private Long latencyMs;
    private String httpMethod;
    private Integer statusCode;
    private String environment;
    private LocalDateTime timestamp;

    // Anomaly-specific fields
    private Double anomalyScore;
    private String anomalyType;
    private String confidence;
    private String severity;
    private LocalDateTime detectedAt;
    private String detectedBy;
}
