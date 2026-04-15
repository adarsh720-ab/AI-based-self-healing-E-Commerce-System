package com.ecommerce.action.repository;

import com.ecommerce.action.model.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    List<ActionLog> findByServiceIdOrderByExecutedAtDesc(String serviceId);

    List<ActionLog> findByActionTypeAndExecutedAtAfter(String actionType, LocalDateTime since);

    long countByServiceIdAndExecutedAtAfter(String serviceId, LocalDateTime since);
}
