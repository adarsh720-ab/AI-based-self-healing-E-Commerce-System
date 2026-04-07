package com.ecommerce.ai.repository;

import com.ecommerce.ai.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findByServiceIdOrderByAnalysedAtDesc(String serviceId);
    List<Incident> findBySeverityOrderByAnalysedAtDesc(Incident.Severity severity);
    List<Incident> findTop10ByOrderByAnalysedAtDesc();
}