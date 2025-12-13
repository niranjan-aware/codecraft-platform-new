package com.codecraft.analysis.service;

import com.codecraft.analysis.entity.ProjectMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
public class CodeMetricsService {

    public ProjectMetrics calculateMetrics(UUID projectId, UUID reportId, String projectPath) {
        ProjectMetrics metrics = new ProjectMetrics();
        metrics.setProjectId(projectId);
        metrics.setAnalysisReportId(reportId);

        try {
            int totalLines = 0;
            int fileCount = 0;

            try (Stream<Path> paths = Files.walk(Path.of(projectPath))) {
                for (Path path : paths.filter(Files::isRegularFile).toList()) {
                    if (isSourceFile(path.toString())) {
                        totalLines += countLines(path.toFile());
                        fileCount++;
                    }
                }
            }

            metrics.setLinesOfCode(totalLines);
            
            double complexityScore = calculateComplexityScore(totalLines, fileCount);
            metrics.setComplexityScore(complexityScore);
            
            metrics.setMaintainabilityRating(calculateMaintainabilityRating(complexityScore));
            metrics.setReliabilityRating("A");
            metrics.setSecurityRating("A");
            
            metrics.setCodeSmells(0);
            metrics.setBugs(0);
            metrics.setVulnerabilities(0);
            metrics.setSecurityHotspots(0);
            metrics.setCoveragePercentage(0.0);
            metrics.setDuplicationPercentage(0.0);
            metrics.setTechnicalDebt(0);

        } catch (Exception e) {
            log.error("Error calculating metrics", e);
        }

        return metrics;
    }

    private boolean isSourceFile(String path) {
        return path.endsWith(".js") || path.endsWith(".jsx") ||
               path.endsWith(".ts") || path.endsWith(".tsx") ||
               path.endsWith(".py") || path.endsWith(".java") ||
               path.endsWith(".html") || path.endsWith(".css");
    }

    private int countLines(File file) throws IOException {
        return (int) Files.lines(file.toPath()).count();
    }

    private double calculateComplexityScore(int totalLines, int fileCount) {
        if (fileCount == 0) return 0.0;
        double avgLinesPerFile = (double) totalLines / fileCount;
        return Math.min(10.0, avgLinesPerFile / 50.0);
    }

    private String calculateMaintainabilityRating(double complexityScore) {
        if (complexityScore < 2.0) return "A";
        if (complexityScore < 4.0) return "B";
        if (complexityScore < 6.0) return "C";
        if (complexityScore < 8.0) return "D";
        return "E";
    }
}
