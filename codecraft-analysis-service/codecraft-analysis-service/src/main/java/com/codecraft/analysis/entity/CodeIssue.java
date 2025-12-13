package com.codecraft.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "code_issues", indexes = {
    @Index(name = "idx_report_id", columnList = "analysis_report_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID analysisReportId;

    @Column(nullable = false, length = 500)
    private String filePath;

    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false, length = 200)
    private String ruleId;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 5000)
    private String suggestedFix;

    private Boolean aiFixAvailable = false;

    @Column(length = 2000)
    private String codeSnippet;

    public enum Severity {
        BLOCKER, CRITICAL, MAJOR, MINOR, INFO
    }
}
