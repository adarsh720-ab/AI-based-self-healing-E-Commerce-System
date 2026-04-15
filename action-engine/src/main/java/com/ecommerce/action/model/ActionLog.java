package com.ecommerce.action.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private String serviceId;

    @Column(name = "anomaly_type")
    private String anomalyType;

    @Column(name = "anomaly_score")
    private Double anomalyScore;

    @Column(name = "severity")
    private String severity;

    @Column(name = "confidence")
    private String confidence;

    /**
     * Action taken: RESTART | SCALE_UP | LOG_AND_MONITOR
     */
    @Column(name = "action_type", nullable = false)
    private String actionType;

    /**
     * Result: STUB_EXECUTED | CONTAINER_RESTARTED | LOGGED
     */
    @Column(name = "outcome")
    private String outcome;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;
}
