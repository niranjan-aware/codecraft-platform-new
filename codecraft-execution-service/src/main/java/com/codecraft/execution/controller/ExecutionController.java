package com.codecraft.execution.controller;

import com.codecraft.execution.dto.ExecutionRequest;
import com.codecraft.execution.dto.ExecutionResponse;
import com.codecraft.execution.dto.LogMessage;
import com.codecraft.execution.service.ExecutionService;
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
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping
    public ResponseEntity<ExecutionResponse> startExecution(
            @Valid @RequestBody ExecutionRequest request,
            @RequestHeader("X-User-Id") String userId) {
        ExecutionResponse response = executionService.startExecution(request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionResponse> getExecution(
            @PathVariable UUID executionId,
            @RequestHeader("X-User-Id") String userId) {
        ExecutionResponse response = executionService.getExecution(executionId, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ExecutionResponse>> getProjectExecutions(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId) {
        List<ExecutionResponse> executions = executionService.getExecutionsByProject(
            projectId, 
            UUID.fromString(userId)
        );
        return ResponseEntity.ok(executions);
    }

    @DeleteMapping("/{executionId}")
    public ResponseEntity<Void> stopExecution(
            @PathVariable UUID executionId,
            @RequestHeader("X-User-Id") String userId) {
        executionService.stopExecution(executionId, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{executionId}/logs")
    public ResponseEntity<List<LogMessage>> getLogs(
            @PathVariable UUID executionId,
            @RequestHeader("X-User-Id") String userId) {
        List<LogMessage> logs = executionService.getLogs(executionId, UUID.fromString(userId));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Execution Service is running");
    }
}
