package com.ecommerce.commons.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        String httpMethod = resolveHttpMethod();

        try {
            Object result = pjp.proceed();
            publishLog("INFO", method, "OK", traceId, null, null,
                    System.currentTimeMillis() - start, httpMethod, 200);
            return result;
        } catch (Exception ex) {
            int statusCode = resolveStatusCode(ex);
            publishLog("ERROR", method, ex.getMessage(), traceId,
                    ex.getClass().getSimpleName(), truncate(ex),
                    System.currentTimeMillis() - start, httpMethod, statusCode);
            throw ex;
        }
    }

    /**
     * Resolves HTTP method from current request context.
     * Returns "INTERNAL" for Kafka consumers and scheduled tasks
     * that have no HTTP request context.
     */
    private String resolveHttpMethod() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getMethod();
            }
        } catch (Exception ignored) {}
        return "INTERNAL";
    }

    /**
     * Maps exception type to HTTP status code.
     * Mirrors what your GlobalExceptionHandler returns.
     */
    private int resolveStatusCode(Exception ex) {
        String name = ex.getClass().getSimpleName();
        return switch (name) {
            case "ResourceNotFoundException"  -> 404;
            case "ConflictException"          -> 409;
            case "UnauthorizedException"      -> 401;
            case "ValidationException"        -> 400;
            case "InsufficientStockException" -> 422;
            case "TokenExpiredException"      -> 401;
            case "RetryableException"         -> 503;
            case "TimeoutException"           -> 504;
            default                           -> 500;
        };
    }

    private void publishLog(String level, String method, String message,
                            String traceId, String errorCode, String stackTrace,
                            long latencyMs, String httpMethod, int statusCode) {
        try {
            Map<String, Object> entry = new HashMap<>();
            entry.put("serviceId",   serviceName);
            entry.put("level",       level);
            entry.put("method",      method);
            entry.put("message",     message);
            entry.put("traceId",     traceId);
            entry.put("latencyMs",   latencyMs);
            entry.put("errorCode",   errorCode);
            entry.put("stackTrace",  stackTrace);
            entry.put("httpMethod",  httpMethod);
            entry.put("statusCode",  statusCode);
            entry.put("environment", "prod");
            entry.put("timestamp",   LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")));
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