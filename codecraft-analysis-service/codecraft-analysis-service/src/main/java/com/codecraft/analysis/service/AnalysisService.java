package com.codecraft.analysis.service;

import com.codecraft.analysis.dto.*;
import com.codecraft.analysis.entity.*;
import com.codecraft.analysis.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final AnalysisReportRepository reportRepository;
    private final CodeIssueRepository issueRepository;
    private final ProjectMetricsRepository metricsRepository;
    private final DependencyRepository dependencyRepository;
    private final StorageService storageService;
    private final SemgrepService semgrepService;
    private final CodeMetricsService metricsService;
    private final DependencyService dependencyService;

    @Transactional
    public AnalysisResponse startAnalysis(AnalysisRequest request, UUID userId) {
        AnalysisReport report = new AnalysisReport();
        report.setProjectId(request.getProjectId());
        report.setUserId(userId);
        report.setAnalysisType(request.getAnalysisType());
        report.setStatus(AnalysisReport.AnalysisStatus.PENDING);
        
        report = reportRepository.save(report);

        analyzeAsync(report.getId());

        return mapToResponse(report);
    }

    @Async
    public void analyzeAsync(UUID reportId) {
        AnalysisReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        String projectPath = null;

        try {
            report.setStatus(AnalysisReport.AnalysisStatus.RUNNING);
            reportRepository.save(report);

            projectPath = storageService.downloadProjectFiles(report.getProjectId());
            log.info("Project files downloaded to: {}", projectPath);

            String language = detectLanguage(projectPath);
            log.info("Detected language: {}", language);

            List<CodeIssue> securityIssues = semgrepService.runSemgrep(
                report.getId(),
                projectPath,
                language
            );
            
            if (!securityIssues.isEmpty()) {
                issueRepository.saveAll(securityIssues);
                log.info("Found {} security issues", securityIssues.size());
            }

            ProjectMetrics metrics = metricsService.calculateMetrics(
                report.getProjectId(),
                report.getId(),
                projectPath
            );
            
            metrics.setVulnerabilities(securityIssues.size());
            metricsRepository.save(metrics);
            log.info("Metrics calculated: {} lines of code", metrics.getLinesOfCode());

            List<Dependency> dependencies = dependencyService.analyzeDependencies(
                report.getProjectId(),
                report.getId(),
                projectPath,
                language
            );
            
            if (!dependencies.isEmpty()) {
                dependencyRepository.saveAll(dependencies);
                log.info("Found {} dependencies", dependencies.size());
            }

            report.setStatus(AnalysisReport.AnalysisStatus.COMPLETED);
            report.setCompletedAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Analysis failed", e);
            report.setStatus(AnalysisReport.AnalysisStatus.FAILED);
            report.setErrorMessage(e.getMessage());
            report.setCompletedAt(LocalDateTime.now());
        } finally {
            reportRepository.save(report);
            
            if (projectPath != null) {
                storageService.cleanupProjectFiles(report.getProjectId());
            }
        }
    }

    public AnalysisResponse getAnalysis(UUID reportId, UUID userId) {
        AnalysisReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return mapToResponse(report);
    }

    public List<AnalysisResponse> getProjectAnalyses(UUID projectId, UUID userId) {
        return reportRepository.findByProjectIdOrderByStartedAtDesc(projectId).stream()
            .filter(r -> r.getUserId().equals(userId))
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<CodeIssueResponse> getIssues(UUID reportId, UUID userId) {
        AnalysisReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return issueRepository.findByAnalysisReportId(reportId).stream()
            .map(this::mapIssueToResponse)
            .collect(Collectors.toList());
    }

    public MetricsResponse getMetrics(UUID reportId, UUID userId) {
        AnalysisReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        ProjectMetrics metrics = metricsRepository.findByAnalysisReportId(reportId)
            .orElseThrow(() -> new RuntimeException("Metrics not found"));

        return mapMetricsToResponse(metrics);
    }

    public List<DependencyResponse> getDependencies(UUID reportId, UUID userId) {
        AnalysisReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return dependencyRepository.findByAnalysisReportId(reportId).stream()
            .map(this::mapDependencyToResponse)
            .collect(Collectors.toList());
    }

    private String detectLanguage(String projectPath) {
        File dir = new File(projectPath);
        if (new File(dir, "package.json").exists()) return "NODEJS";
        if (new File(dir, "requirements.txt").exists()) return "PYTHON";
        if (new File(dir, "pom.xml").exists()) return "JAVA";
        return "UNKNOWN";
    }

    private AnalysisResponse mapToResponse(AnalysisReport report) {
        return new AnalysisResponse(
            report.getId(),
            report.getProjectId(),
            report.getAnalysisType(),
            report.getStatus(),
            report.getStartedAt(),
            report.getCompletedAt(),
            report.getErrorMessage()
        );
    }

    private CodeIssueResponse mapIssueToResponse(CodeIssue issue) {
        return new CodeIssueResponse(
            issue.getId(),
            issue.getFilePath(),
            issue.getLineNumber(),
            issue.getSeverity(),
            issue.getRuleId(),
            issue.getMessage(),
            issue.getSuggestedFix(),
            issue.getAiFixAvailable(),
            issue.getCodeSnippet()
        );
    }

    private MetricsResponse mapMetricsToResponse(ProjectMetrics metrics) {
        return new MetricsResponse(
            metrics.getId(),
            metrics.getProjectId(),
            metrics.getLinesOfCode(),
            metrics.getCodeSmells(),
            metrics.getBugs(),
            metrics.getVulnerabilities(),
            metrics.getSecurityHotspots(),
            metrics.getCoveragePercentage(),
            metrics.getDuplicationPercentage(),
            metrics.getComplexityScore(),
            metrics.getMaintainabilityRating(),
            metrics.getReliabilityRating(),
            metrics.getSecurityRating(),
            metrics.getTechnicalDebt()
        );
    }

    private DependencyResponse mapDependencyToResponse(Dependency dep) {
        return new DependencyResponse(
            dep.getId(),
            dep.getName(),
            dep.getVersion(),
            dep.getDependencyType(),
            dep.getVulnerabilities(),
            dep.getLicense(),
            dep.getOutdated(),
            dep.getLatestVersion()
        );
    }
}
