package com.ecommerce.ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "anomaly_type")
    private String anomalyType;

    @Column(name = "anomaly_score")
    private Double anomalyScore;

    @Column(name = "confidence")
    private String confidence;

    @Column(name = "error_code")
    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "suggested_fix", columnDefinition = "TEXT")
    private String suggestedFix;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "detected_at")
    private String detectedAt;

    @Column(name = "analysed_at")
    private LocalDateTime analysedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.OPEN;

    public enum Severity {
        CRITICAL, WARNING, LOW
    }

    public enum IncidentStatus {
        OPEN, RESOLVED, IGNORED
    }
}