package com.ecommerce.action.action;

import com.ecommerce.action.event.AnomalyEvent;
import com.ecommerce.action.service.ActionDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestartAction {

    private final ActionDispatcher dispatcher;

    @Value("${action.docker.stub-mode:true}")
    private boolean stubMode;

    public void execute(AnomalyEvent event) {
        String serviceId = event.getServiceId();

        if (stubMode) {
            // ── STUB MODE ────────────────────────────────────────────────────────
            // To activate real Docker restart:
            //   1. Set action.docker.stub-mode=false in application.yml
            //   2. Mount Docker socket in docker-compose:
            //        volumes:
            //          - /var/run/docker.sock:/var/run/docker.sock
            //   3. Add docker-java dependency to pom.xml
            //   4. Replace stub block with:
            //        DockerClient docker = DockerClientBuilder.getInstance().build();
            //        docker.restartContainerCmd(serviceId).exec();
            // ─────────────────────────────────────────────────────────────────────
            log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.warn("[RESTART STUB] Would restart container for service : {}", serviceId);
            log.warn("[RESTART STUB] anomalyType={} score={} traceId={}",
                    event.getAnomalyType(), event.getAnomalyScore(), event.getTraceId());
            log.warn("[RESTART STUB] Set action.docker.stub-mode=false to activate.");
            log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            dispatcher.recordAction(event, "RESTART", "STUB_EXECUTED");
        } else {
            log.warn("[RESTART] Restarting container for service: {}", serviceId);
            // Real Docker restart goes here
            dispatcher.recordAction(event, "RESTART", "CONTAINER_RESTARTED");
        }
    }
}
