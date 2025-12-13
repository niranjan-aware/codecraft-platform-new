package com.codecraft.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_metrics")
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

    private Long linesOfCode = 0L;

    private Integer codeSmells = 0;

    private Integer bugs = 0;

    private Integer vulnerabilities = 0;

    private Integer securityHotspots = 0;

    private Double coveragePercentage = 0.0;

    private Double duplicationPercentage = 0.0;

    private Integer complexityScore = 0;

    @Enumerated(EnumType.STRING)
    private Rating maintainabilityRating = Rating.A;

    @Enumerated(EnumType.STRING)
    private Rating reliabilityRating = Rating.A;

    @Enumerated(EnumType.STRING)
    private Rating securityRating = Rating.A;

    private Integer technicalDebt = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum Rating {
        A, B, C, D, E
    }
}
