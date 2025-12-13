package com.codecraft.analysis.repository;

import com.codecraft.analysis.entity.ProjectMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMetricsRepository extends JpaRepository<ProjectMetrics, UUID> {
    Optional<ProjectMetrics> findByAnalysisReportId(UUID reportId);
    List<ProjectMetrics> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
