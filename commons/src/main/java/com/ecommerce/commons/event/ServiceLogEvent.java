package com.ecommerce.commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ServiceLogEvent {
    private String serviceId;
    private String level;
    private String method;
    private String message;
    private String traceId;
    private long latencyMs;
    private String errorCode;
    private String stackTrace;
    private LocalDateTime timestamp;
}

