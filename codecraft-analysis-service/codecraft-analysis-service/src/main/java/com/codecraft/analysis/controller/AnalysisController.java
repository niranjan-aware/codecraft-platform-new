package com.codecraft.analysis.controller;

import com.codecraft.analysis.dto.*;
import com.codecraft.analysis.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<AnalysisResponse> startAnalysis(
            @Valid @RequestBody AnalysisRequest request,
            @RequestHeader("X-User-Id") String userId) {
        AnalysisResponse response = analysisService.startAnalysis(request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<AnalysisResponse> getAnalysis(
            @PathVariable UUID reportId,
            @RequestHeader("X-User-Id") String userId) {
        AnalysisResponse response = analysisService.getAnalysis(reportId, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AnalysisResponse>> getProjectAnalyses(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId) {
        List<AnalysisResponse> analyses = analysisService.getProjectAnalyses(
            projectId,
            UUID.fromString(userId)
        );
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/{reportId}/issues")
    public ResponseEntity<List<CodeIssueResponse>> getIssues(
            @PathVariable UUID reportId,
            @RequestHeader("X-User-Id") String userId) {
        List<CodeIssueResponse> issues = analysisService.getIssues(reportId, UUID.fromString(userId));
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/{reportId}/metrics")
    public ResponseEntity<MetricsResponse> getMetrics(
            @PathVariable UUID reportId,
            @RequestHeader("X-User-Id") String userId) {
        MetricsResponse metrics = analysisService.getMetrics(reportId, UUID.fromString(userId));
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/{reportId}/dependencies")
    public ResponseEntity<List<DependencyResponse>> getDependencies(
            @PathVariable UUID reportId,
            @RequestHeader("X-User-Id") String userId) {
        List<DependencyResponse> dependencies = analysisService.getDependencies(
            reportId,
            UUID.fromString(userId)
        );
        return ResponseEntity.ok(dependencies);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analysis Service is running");
    }
}
