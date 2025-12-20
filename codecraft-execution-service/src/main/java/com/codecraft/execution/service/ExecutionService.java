package com.codecraft.execution.service;

import com.codecraft.execution.dto.ExecutionRequest;
import com.codecraft.execution.dto.ExecutionResponse;
import com.codecraft.execution.dto.LogMessage;
import com.codecraft.execution.entity.Execution;
import com.codecraft.execution.entity.ExecutionLog;
import com.codecraft.execution.repository.ExecutionLogRepository;
import com.codecraft.execution.repository.ExecutionRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final ExecutionLogRepository logRepository;
    private final DockerService dockerService;
    private final StorageService storageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DockerClient dockerClient;
    private final PortAllocationService portAllocationService;
    private final ProjectTypeDetector projectTypeDetector;
    private final EnvironmentParser environmentParser;

    @Value("${execution.auto-stop-hours:1}")
    private Integer autoStopHours;

    @Transactional
    public ExecutionResponse startExecution(ExecutionRequest request, UUID userId) {
        if (!portAllocationService.canAllocateMore(userId)) {
            throw new RuntimeException("Maximum concurrent containers reached. Please stop one container first.");
        }

        Execution execution = new Execution();
        execution.setProjectId(request.getProjectId());
        execution.setUserId(userId);
        execution.setLanguage(request.getLanguage());
        execution.setStatus(Execution.ExecutionStatus.PENDING);
        
        execution = executionRepository.save(execution);

        executeAsync(execution.getId());

        return mapToResponse(execution);
    }

    @Async
    public void executeAsync(UUID executionId) {
        Execution execution = executionRepository.findById(executionId)
            .orElseThrow(() -> new RuntimeException("Execution not found"));

        String containerId = null;
        String workingDir = null;
        Integer allocatedPort = null;

        try {
            execution.setStatus(Execution.ExecutionStatus.RUNNING);
            execution.setStartedAt(LocalDateTime.now());
            execution.setAutoStopAt(LocalDateTime.now().plusHours(autoStopHours));
            executionRepository.save(execution);

            sendLog(execution.getId(), "INFO", "Starting execution...");

            workingDir = storageService.downloadProjectFiles(execution.getProjectId());
            sendLog(execution.getId(), "INFO", "Project files downloaded");

            Execution.ProjectType projectType = projectTypeDetector.detectProjectType(
                workingDir, 
                execution.getLanguage()
            );
            execution.setProjectType(projectType);
            sendLog(execution.getId(), "INFO", "Project type detected: " + projectType);

            Map<String, String> envVars = environmentParser.getAllEnvVars(workingDir);
            envVars.put("NODE_ENV", "production");
            envVars.put("PYTHONUNBUFFERED", "1");

            Integer containerPort = null;
            if (projectType == Execution.ProjectType.SERVER) {
                Integer defaultPort = environmentParser.getDefaultPort(execution.getLanguage().name());
                containerPort = environmentParser.getPort(workingDir, defaultPort);
                execution.setContainerPort(containerPort);

                allocatedPort = portAllocationService.allocatePort(execution.getUserId(), containerPort);
                execution.setHostPort(allocatedPort);

                String serverIp = getServerIp();
                String publicUrl = String.format("http://%s:%d", serverIp, allocatedPort);
                execution.setPublicUrl(publicUrl);

                sendLog(execution.getId(), "INFO", "Port mapping: localhost:" + allocatedPort + " -> container:" + containerPort);
                sendLog(execution.getId(), "INFO", "Access your application at: " + publicUrl);
            }

            executionRepository.save(execution);

            containerId = dockerService.createContainer(
                execution.getLanguage(),
                workingDir,
                envVars,
                allocatedPort,
                containerPort,
                projectType == Execution.ProjectType.SERVER
            );
            
            execution.setContainerId(containerId);
            executionRepository.save(execution);

            dockerService.startContainer(containerId);
            sendLog(execution.getId(), "INFO", "Container started: " + containerId);

            executeLanguageSpecificCommands(execution, containerId, projectType);

            if (projectType == Execution.ProjectType.SCRIPT) {
                Thread.sleep(5000);
                
                Integer exitCode = dockerService.getExitCode(containerId);
                execution.setExitCode(exitCode);
                
                if (exitCode != null && exitCode == 0) {
                    execution.setStatus(Execution.ExecutionStatus.SUCCESS);
                    sendLog(execution.getId(), "INFO", "Script execution completed successfully");
                } else {
                    execution.setStatus(Execution.ExecutionStatus.FAILED);
                    sendLog(execution.getId(), "ERROR", "Script execution failed with exit code: " + exitCode);
                }
                
                execution.setCompletedAt(LocalDateTime.now());
            } else {
                execution.setStatus(Execution.ExecutionStatus.RUNNING);
                sendLog(execution.getId(), "INFO", "Server is running. It will auto-stop after " + autoStopHours + " hour(s)");
            }

            executionRepository.save(execution);

        } catch (Exception e) {
            log.error("Execution failed", e);
            execution.setStatus(Execution.ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
            execution.setCompletedAt(LocalDateTime.now());
            sendLog(execution.getId(), "ERROR", "Execution error: " + e.getMessage());
            executionRepository.save(execution);
        } finally {
            if (execution.getProjectType() == Execution.ProjectType.SCRIPT && containerId != null) {
                try {
                    Thread.sleep(2000);
                    dockerService.stopContainer(containerId);
                    dockerService.removeContainer(containerId);
                    if (allocatedPort != null) {
                        portAllocationService.releasePort(execution.getUserId(), allocatedPort);
                    }
                    sendLog(execution.getId(), "INFO", "Container cleaned up");
                } catch (Exception e) {
                    log.error("Failed to cleanup container", e);
                }
            }

            if (workingDir != null && execution.getProjectType() == Execution.ProjectType.SCRIPT) {
                storageService.cleanupProjectFiles(execution.getProjectId());
            }
        }
    }

    private void executeLanguageSpecificCommands(Execution execution, String containerId, Execution.ProjectType projectType) {
        try {
            switch (execution.getLanguage()) {
                case NODEJS -> executeNodeJs(execution, containerId, projectType);
                case PYTHON -> executePython(execution, containerId, projectType);
                case JAVA -> executeJava(execution, containerId, projectType);
                case HTML_CSS_JS -> executeHtmlCssJs(execution, containerId);
            }
        } catch (Exception e) {
            log.error("Failed to execute commands", e);
            sendLog(execution.getId(), "ERROR", "Command execution failed: " + e.getMessage());
        }
    }

    private void executeNodeJs(Execution execution, String containerId, Execution.ProjectType projectType) throws Exception {
        sendLog(execution.getId(), "INFO", "Checking for package.json...");
        
        executeCommand(execution, containerId, 
            new String[]{"sh", "-c", "if [ -f package.json ]; then echo 'Found package.json'; npm install; fi"}, 120);
        
        sendLog(execution.getId(), "INFO", "Running application...");
        
        if (projectType == Execution.ProjectType.SERVER) {
            executeCommandAsync(execution, containerId,
                new String[]{"sh", "-c", "if [ -f package.json ]; then npm start; elif [ -f server.js ]; then node server.js; elif [ -f index.js ]; then node index.js; fi"});
        } else {
            executeCommand(execution, containerId,
                new String[]{"sh", "-c", "if [ -f index.js ]; then node index.js; elif [ -f app.js ]; then node app.js; fi"}, 60);
        }
    }

    private void executePython(Execution execution, String containerId, Execution.ProjectType projectType) throws Exception {
        sendLog(execution.getId(), "INFO", "Checking for requirements.txt...");
        
        executeCommand(execution, containerId,
            new String[]{"sh", "-c", "if [ -f requirements.txt ]; then pip install -r requirements.txt; fi"}, 120);
        
        sendLog(execution.getId(), "INFO", "Running application...");
        
        if (projectType == Execution.ProjectType.SERVER) {
            executeCommandAsync(execution, containerId,
                new String[]{"sh", "-c", "if [ -f app.py ]; then python app.py; elif [ -f main.py ]; then python main.py; fi"});
        } else {
            executeCommand(execution, containerId,
                new String[]{"sh", "-c", "if [ -f main.py ]; then python main.py; elif [ -f app.py ]; then python app.py; fi"}, 60);
        }
    }

    private void executeJava(Execution execution, String containerId, Execution.ProjectType projectType) throws Exception {
        sendLog(execution.getId(), "INFO", "Building Java project...");
        
        executeCommand(execution, containerId,
            new String[]{"sh", "-c", "if [ -f pom.xml ]; then mvn clean package -DskipTests; fi"}, 300);
        
        sendLog(execution.getId(), "INFO", "Running application...");
        
        if (projectType == Execution.ProjectType.SERVER) {
            executeCommandAsync(execution, containerId,
                new String[]{"sh", "-c", "java -jar target/*.jar"});
        } else {
            executeCommand(execution, containerId,
                new String[]{"sh", "-c", "java -jar target/*.jar"}, 60);
        }
    }

    private void executeHtmlCssJs(Execution execution, String containerId) throws Exception {
        sendLog(execution.getId(), "INFO", "Starting HTTP server...");
        
        executeCommandAsync(execution, containerId,
            new String[]{"sh", "-c", "python3 -m http.server " + execution.getContainerPort()});
    }

    private void executeCommand(Execution execution, String containerId, String[] command, int timeoutSeconds) throws Exception {
        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
            .withCmd(command)
            .withAttachStdout(true)
            .withAttachStderr(true)
            .exec();

        dockerClient.execStartCmd(execCreateCmd.getId())
            .exec(new ExecStartResultCallback() {
                @Override
                public void onNext(Frame frame) {
                    String output = new String(frame.getPayload()).trim();
                    if (!output.isEmpty()) {
                        String level = frame.getStreamType().name().equals("STDERR") ? "ERROR" : "INFO";
                        sendLog(execution.getId(), level, output);
                    }
                }
            })
            .awaitCompletion(timeoutSeconds, TimeUnit.SECONDS);
    }

    private void executeCommandAsync(Execution execution, String containerId, String[] command) {
        try {
            ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd(command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

            dockerClient.execStartCmd(execCreateCmd.getId())
                .exec(new ExecStartResultCallback() {
                    @Override
                    public void onNext(Frame frame) {
                        String output = new String(frame.getPayload()).trim();
                        if (!output.isEmpty()) {
                            String level = frame.getStreamType().name().equals("STDERR") ? "ERROR" : "INFO";
                            sendLog(execution.getId(), level, output);
                        }
                    }
                });
        } catch (Exception e) {
            log.error("Failed to execute async command", e);
        }
    }

    private void sendLog(UUID executionId, String level, String message) {
        ExecutionLog log = new ExecutionLog();
        log.setExecutionId(executionId);
        log.setLogLevel(ExecutionLog.LogLevel.valueOf(level));
        log.setMessage(message);
        logRepository.save(log);

        LogMessage logMessage = new LogMessage(level, message, LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/execution/" + executionId, logMessage);
    }

    private String getServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("Failed to get server IP, using localhost", e);
            return "localhost";
        }
    }

    public ExecutionResponse getExecution(UUID executionId, UUID userId) {
        Execution execution = executionRepository.findById(executionId)
            .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (!execution.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return mapToResponse(execution);
    }

    public List<ExecutionResponse> getExecutionsByProject(UUID projectId, UUID userId) {
        return executionRepository.findByProjectId(projectId).stream()
            .filter(e -> e.getUserId().equals(userId))
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional
    public void stopExecution(UUID executionId, UUID userId) {
        Execution execution = executionRepository.findById(executionId)
            .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (!execution.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (execution.getContainerId() != null) {
            dockerService.stopContainer(execution.getContainerId());
            dockerService.removeContainer(execution.getContainerId());
            
            if (execution.getHostPort() != null) {
                portAllocationService.releasePort(userId, execution.getHostPort());
            }
        }

        execution.setStatus(Execution.ExecutionStatus.STOPPED);
        execution.setCompletedAt(LocalDateTime.now());
        executionRepository.save(execution);

        sendLog(executionId, "INFO", "Execution stopped by user");
    }

    public List<LogMessage> getLogs(UUID executionId, UUID userId) {
        Execution execution = executionRepository.findById(executionId)
            .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (!execution.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return logRepository.findByExecutionIdOrderByTimestampAsc(executionId).stream()
            .map(log -> new LogMessage(
                log.getLogLevel().name(),
                log.getMessage(),
                log.getTimestamp()
            ))
            .toList();
    }

    public List<ExecutionResponse> getRunningExecutions(UUID userId) {
        return executionRepository.findByUserId(userId).stream()
            .filter(e -> e.getStatus() == Execution.ExecutionStatus.RUNNING)
            .map(this::mapToResponse)
            .toList();
    }

    private ExecutionResponse mapToResponse(Execution execution) {
        return new ExecutionResponse(
            execution.getId(),
            execution.getProjectId(),
            execution.getContainerId(),
            execution.getStatus(),
            execution.getLanguage(),
            execution.getProjectType(),
            execution.getStartedAt(),
            execution.getCompletedAt(),
            execution.getAutoStopAt(),
            execution.getHostPort(),
            execution.getContainerPort(),
            execution.getPublicUrl(),
            execution.getCpuUsage(),
            execution.getMemoryUsage(),
            execution.getExitCode(),
            execution.getErrorMessage(),
            execution.getCreatedAt()
        );
    }
}
