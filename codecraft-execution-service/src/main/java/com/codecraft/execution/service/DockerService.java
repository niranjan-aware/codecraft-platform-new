package com.codecraft.execution.service;

import com.codecraft.execution.entity.Execution;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerService {

    private final DockerClient dockerClient;

    @Value("${execution.container.memory-limit}")
    private String memoryLimit;

    @Value("${execution.container.cpu-limit}")
    private Double cpuLimit;

    @Value("${execution.container.network-disabled:false}")
    private Boolean networkDisabled;

    private static final Map<Execution.Language, String> LANGUAGE_IMAGES = Map.of(
        Execution.Language.NODEJS, "node:18-alpine",
        Execution.Language.PYTHON, "python:3.11-alpine",
        Execution.Language.JAVA, "eclipse-temurin:21-jdk-alpine",
        Execution.Language.HTML_CSS_JS, "nginx:alpine"
    );

    public void ensureImageExists(Execution.Language language) {
        String imageName = LANGUAGE_IMAGES.get(language);
        
        try {
            dockerClient.inspectImageCmd(imageName).exec();
            log.info("Image {} already exists", imageName);
        } catch (Exception e) {
            log.info("Pulling image {}", imageName);
            try {
                dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(5, TimeUnit.MINUTES);
                log.info("Image {} pulled successfully", imageName);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to pull image", ie);
            }
        }
    }

    public String createContainer(
            Execution.Language language, 
            String workingDir, 
            Map<String, String> env,
            Integer hostPort,
            Integer containerPort,
            boolean isServer) {
        
        ensureImageExists(language);
        
        String imageName = LANGUAGE_IMAGES.get(language);
        
        HostConfig hostConfig = HostConfig.newHostConfig()
            .withMemory(parseMemoryLimit(memoryLimit))
            .withNanoCPUs((long) (cpuLimit * 1_000_000_000))
            .withBinds(new Bind(workingDir, new Volume("/workspace")))
            .withAutoRemove(false);

        if (isServer && hostPort != null && containerPort != null) {
            hostConfig.withPortBindings(
                new PortBinding(
                    Ports.Binding.bindPort(hostPort),
                    ExposedPort.tcp(containerPort)
                )
            );
            hostConfig.withNetworkMode("bridge");
            log.info("Port mapping: {}:{}", hostPort, containerPort);
        } else {
            hostConfig.withNetworkMode(networkDisabled ? "none" : "bridge");
        }

        List<String> envList = new ArrayList<>();
        env.forEach((key, value) -> envList.add(key + "=" + value));

        ExposedPort[] exposedPorts = isServer && containerPort != null 
            ? new ExposedPort[]{ExposedPort.tcp(containerPort)}
            : new ExposedPort[]{};

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
            .withHostConfig(hostConfig)
            .withWorkingDir("/workspace")
            .withEnv(envList)
            .withTty(true)
            .withAttachStdout(true)
            .withAttachStderr(true)
            .withExposedPorts(exposedPorts)
            .withCmd(getDefaultCommand(language))
            .exec();

        log.info("Container created: {}", container.getId());
        return container.getId();
    }

    public void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
        log.info("Container started: {}", containerId);
    }

    public void stopContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId)
                .withTimeout(10)
                .exec();
            log.info("Container stopped: {}", containerId);
        } catch (Exception e) {
            log.error("Error stopping container", e);
        }
    }

    public void removeContainer(String containerId) {
        try {
            dockerClient.removeContainerCmd(containerId)
                .withForce(true)
                .exec();
            log.info("Container removed: {}", containerId);
        } catch (Exception e) {
            log.error("Error removing container", e);
        }
    }

    public boolean isContainerRunning(String containerId) {
        try {
            var inspection = dockerClient.inspectContainerCmd(containerId).exec();
            return Boolean.TRUE.equals(inspection.getState().getRunning());
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getExitCode(String containerId) {
        try {
            var inspection = dockerClient.inspectContainerCmd(containerId).exec();
            return inspection.getState().getExitCodeLong().intValue();
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Long> getContainerStats(String containerId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("cpu", 0L);
        stats.put("memory", 0L);
        return stats;
    }

    private String[] getDefaultCommand(Execution.Language language) {
        return switch (language) {
            case NODEJS -> new String[]{"/bin/sh", "-c", "sleep infinity"};
            case PYTHON -> new String[]{"/bin/sh", "-c", "sleep infinity"};
            case JAVA -> new String[]{"/bin/sh", "-c", "sleep infinity"};
            case HTML_CSS_JS -> new String[]{"/bin/sh", "-c", "sleep infinity"};
        };
    }

    private Long parseMemoryLimit(String limit) {
        limit = limit.toLowerCase();
        if (limit.endsWith("m")) {
            return Long.parseLong(limit.substring(0, limit.length() - 1)) * 1024 * 1024;
        } else if (limit.endsWith("g")) {
            return Long.parseLong(limit.substring(0, limit.length() - 1)) * 1024 * 1024 * 1024;
        }
        return Long.parseLong(limit);
    }
}
