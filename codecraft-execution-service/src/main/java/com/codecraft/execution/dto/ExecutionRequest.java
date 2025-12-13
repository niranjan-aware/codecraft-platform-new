package com.codecraft.execution.dto;

import com.codecraft.execution.entity.Execution;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ExecutionRequest {
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    @NotNull(message = "Language is required")
    private Execution.Language language;
    
    private String command;
}
