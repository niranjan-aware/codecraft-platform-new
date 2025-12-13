package com.codecraft.analysis.repository;

import com.codecraft.analysis.entity.CodeIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeIssueRepository extends JpaRepository<CodeIssue, UUID> {
    List<CodeIssue> findByAnalysisReportId(UUID reportId);
    void deleteByAnalysisReportId(UUID reportId);
}
