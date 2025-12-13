package com.codecraft.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class MetricsResponse {
    private UUID id;
    private UUID projectId;
    private Integer linesOfCode;
    private Integer codeSmells;
    private Integer bugs;
    private Integer vulnerabilities;
    private Integer securityHotspots;
    private Double coveragePercentage;
    private Double duplicationPercentage;
    private Double complexityScore;
    private String maintainabilityRating;
    private String reliabilityRating;
    private String securityRating;
    private Integer technicalDebt;
}
