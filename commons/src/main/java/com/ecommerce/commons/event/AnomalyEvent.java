package com.ecommerce.commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyEvent {

    private String traceId;
    private String serviceId;
    private String method;
    private String level;
    private String errorCode;
    private String message;
    private Long latencyMs;
    private String httpMethod;
    private Integer statusCode;
    private String environment;
    private String originalTimestamp;

    // ML enrichment fields
    private Double anomalyScore;
    private String anomalyType;
    private String confidence;
    private String detectedAt;
    private String detectedBy;
}