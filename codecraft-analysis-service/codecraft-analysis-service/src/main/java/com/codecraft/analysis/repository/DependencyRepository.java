package com.codecraft.analysis.repository;

import com.codecraft.analysis.entity.Dependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DependencyRepository extends JpaRepository<Dependency, UUID> {
    List<Dependency> findByAnalysisReportId(UUID reportId);
    List<Dependency> findByProjectId(UUID projectId);
    void deleteByAnalysisReportId(UUID reportId);
}
