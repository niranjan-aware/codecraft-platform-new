package com.codecraft.execution.service;

import com.codecraft.execution.entity.Execution;
import com.codecraft.execution.repository.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionCleanupService {

    private final ExecutionRepository executionRepository;
    private final DockerService dockerService;
    private final PortAllocationService portAllocationService;

    /**
     * Runs every minute to check for containers that need auto-stop
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void cleanupExpiredExecutions() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find running SERVER executions that have exceeded auto-stop time
        List<Execution> expiredExecutions = executionRepository
            .findByStatusAndAutoStopAtBefore(Execution.ExecutionStatus.RUNNING, now);

        if (expiredExecutions.isEmpty()) {
            return;
        }

        log.info("Found {} expired executions to cleanup", expiredExecutions.size());

        for (Execution execution : expiredExecutions) {
            try {
                log.info("Auto-stopping execution {} (project: {})", 
                    execution.getId(), execution.getProjectId());

                // Stop and remove container
                if (execution.getContainerId() != null) {
                    if (dockerService.isContainerRunning(execution.getContainerId())) {
                        dockerService.stopContainer(execution.getContainerId());
                    }
                    dockerService.removeContainer(execution.getContainerId());
                }

                // Release port
                if (execution.getHostPort() != null) {
                    portAllocationService.releasePort(execution.getUserId(), execution.getHostPort());
                    log.info("Released port {} for user {}", 
                        execution.getHostPort(), execution.getUserId());
                }

                // Update execution status
                execution.setStatus(Execution.ExecutionStatus.STOPPED);
                execution.setCompletedAt(LocalDateTime.now());
                execution.setErrorMessage("Auto-stopped after timeout");
                executionRepository.save(execution);

                log.info("Successfully auto-stopped execution {}", execution.getId());

            } catch (Exception e) {
                log.error("Failed to cleanup execution {}", execution.getId(), e);
            }
        }
    }

    /**
     * Cleanup orphaned containers (optional - runs less frequently)
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void cleanupOrphanedContainers() {
        List<Execution> runningExecutions = executionRepository
            .findByStatus(Execution.ExecutionStatus.RUNNING);

        for (Execution execution : runningExecutions) {
            if (execution.getContainerId() != null) {
                // Check if container actually exists
                if (!dockerService.isContainerRunning(execution.getContainerId())) {
                    log.warn("Container {} for execution {} is not running, marking as failed", 
                        execution.getContainerId(), execution.getId());
                    
                    execution.setStatus(Execution.ExecutionStatus.FAILED);
                    execution.setCompletedAt(LocalDateTime.now());
                    execution.setErrorMessage("Container stopped unexpectedly");
                    
                    if (execution.getHostPort() != null) {
                        portAllocationService.releasePort(execution.getUserId(), execution.getHostPort());
                    }
                    
                    executionRepository.save(execution);
                }
            }
        }
    }
}
