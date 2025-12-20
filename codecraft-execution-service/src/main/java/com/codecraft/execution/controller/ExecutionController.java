package com.codecraft.execution.controller;

import com.codecraft.execution.dto.ExecutionRequest;
import com.codecraft.execution.dto.ExecutionResponse;
import com.codecraft.execution.dto.LogMessage;
import com.codecraft.execution.service.ExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(executionService.startExecution(request, userId));
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionResponse> getExecution(
            @PathVariable UUID executionId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(executionService.getExecution(executionId, userId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ExecutionResponse>> getExecutionsByProject(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(executionService.getExecutionsByProject(projectId, userId));
    }

    @GetMapping("/{executionId}/logs")
    public ResponseEntity<List<LogMessage>> getLogs(
            @PathVariable UUID executionId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(executionService.getLogs(executionId, userId));
    }

    @PostMapping("/{executionId}/stop")
    public ResponseEntity<Void> stopExecution(
            @PathVariable UUID executionId,
            @RequestHeader("X-User-Id") UUID userId) {
        executionService.stopExecution(executionId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/running")
    public ResponseEntity<List<ExecutionResponse>> getRunningExecutions(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(executionService.getRunningExecutions(userId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Execution Service is running");
    }
}
