package com.ecommerce.commons.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaLogInterceptor {

    private static final String TOPIC = "service-logs";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String serviceName;

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object intercept(ProceedingJoinPoint pjp) throws Throwable {
        long start     = System.currentTimeMillis();
        String method  = pjp.getSignature().toShortString();
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();

        try {
            Object result = pjp.proceed();
            publishLog("INFO", method, "OK", traceId, null, null, System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            publishLog("ERROR", method, ex.getMessage(), traceId,
                    ex.getClass().getSimpleName(), truncate(ex), System.currentTimeMillis() - start);
            throw ex;
        }
    }

    private void publishLog(String level, String method, String message,
                            String traceId, String errorCode, String stackTrace, long latencyMs) {
        try {
            Map<String, Object> entry = new HashMap<>();
            entry.put("serviceId",  serviceName);
            entry.put("level",      level);
            entry.put("method",     method);
            entry.put("message",    message);
            entry.put("traceId",    traceId);
            entry.put("latencyMs",  latencyMs);
            entry.put("errorCode",  errorCode);
            entry.put("stackTrace", stackTrace);
            entry.put("timestamp",  LocalDateTime.now().toString());
            kafkaTemplate.send(TOPIC, serviceName, objectMapper.writeValueAsString(entry));
        } catch (Exception e) {
            log.error("KafkaLogInterceptor failed: {}", e.getMessage());
        }
    }

    private String truncate(Exception ex) {
        StringBuilder sb = new StringBuilder(ex.toString()).append("\n");
        for (StackTraceElement el : ex.getStackTrace()) {
            sb.append("\tat ").append(el).append("\n");
            if (sb.length() > 3000) { sb.append("\t... truncated"); break; }
        }
        return sb.toString();
    }
}
