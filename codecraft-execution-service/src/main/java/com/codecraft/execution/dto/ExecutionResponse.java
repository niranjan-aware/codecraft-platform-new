package com.codecraft.execution.dto;

import com.codecraft.execution.entity.Execution;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ExecutionResponse {
    private UUID id;
    private UUID projectId;
    private String containerId;
    private Execution.ExecutionStatus status;
    private Execution.Language language;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long cpuUsage;
    private Long memoryUsage;
    private Integer exitCode;
    private Integer port;
    private String errorMessage;
    private LocalDateTime createdAt;
}
