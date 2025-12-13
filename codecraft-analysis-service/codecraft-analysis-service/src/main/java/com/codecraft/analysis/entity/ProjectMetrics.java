package com.codecraft.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private UUID analysisReportId;

    private Integer linesOfCode = 0;
    private Integer codeSmells = 0;
    private Integer bugs = 0;
    private Integer vulnerabilities = 0;
    private Integer securityHotspots = 0;
    private Double coveragePercentage = 0.0;
    private Double duplicationPercentage = 0.0;
    private Double complexityScore = 0.0;
    private String maintainabilityRating = "A";
    private String reliabilityRating = "A";
    private String securityRating = "A";
    private Integer technicalDebt = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
