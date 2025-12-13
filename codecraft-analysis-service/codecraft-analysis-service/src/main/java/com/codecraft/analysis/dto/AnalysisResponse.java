package com.codecraft.analysis.dto;

import com.codecraft.analysis.entity.AnalysisReport;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AnalysisResponse {
    private UUID id;
    private UUID projectId;
    private AnalysisReport.AnalysisType analysisType;
    private AnalysisReport.AnalysisStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
}
