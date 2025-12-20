package com.codecraft.execution.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private UUID userId;

    private String containerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status = ExecutionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType projectType = ProjectType.SCRIPT;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime autoStopAt;

    private Integer hostPort;

    private Integer containerPort;

    private String publicUrl;

    private Long cpuUsage;

    private Long memoryUsage;

    private Integer exitCode;

    @Column(length = 5000)
    private String errorMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum ExecutionStatus {
        PENDING, RUNNING, SUCCESS, FAILED, TIMEOUT, STOPPED
    }

    public enum Language {
        NODEJS, PYTHON, JAVA, HTML_CSS_JS
    }

    public enum ProjectType {
        SCRIPT, SERVER
    }
}
