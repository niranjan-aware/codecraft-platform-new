package com.codecraft.analysis.repository;

import com.codecraft.analysis.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, UUID> {
    List<AnalysisReport> findByProjectIdOrderByStartedAtDesc(UUID projectId);
    List<AnalysisReport> findByUserIdOrderByStartedAtDesc(UUID userId);
}
