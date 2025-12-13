package com.codecraft.analysis.dto;

import com.codecraft.analysis.entity.AnalysisReport;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AnalysisRequest {
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    private AnalysisReport.AnalysisType analysisType = AnalysisReport.AnalysisType.FULL;
}
