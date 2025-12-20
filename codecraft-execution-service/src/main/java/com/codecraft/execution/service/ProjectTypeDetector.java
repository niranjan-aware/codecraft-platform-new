package com.codecraft.execution.service;

import com.codecraft.execution.entity.Execution;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
@Slf4j
public class ProjectTypeDetector {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Execution.ProjectType detectProjectType(String workingDir, Execution.Language language) {
        try {
            return switch (language) {
                case NODEJS -> detectNodeJsType(workingDir);
                case PYTHON -> detectPythonType(workingDir);
                case JAVA -> detectJavaType(workingDir);
                case HTML_CSS_JS -> Execution.ProjectType.SERVER;
            };
        } catch (Exception e) {
            log.warn("Failed to detect project type, defaulting to SCRIPT", e);
            return Execution.ProjectType.SCRIPT;
        }
    }

    private Execution.ProjectType detectNodeJsType(String workingDir) throws IOException {
        File packageJson = new File(workingDir, "package.json");
        if (!packageJson.exists()) {
            return Execution.ProjectType.SCRIPT;
        }

        JsonNode root = objectMapper.readTree(packageJson);
        JsonNode scripts = root.get("scripts");
        
        if (scripts != null && scripts.has("start")) {
            String startScript = scripts.get("start").asText();
            if (startScript.contains("server") || 
                startScript.contains("express") || 
                startScript.contains("fastify") ||
                startScript.contains("next") ||
                startScript.contains("nuxt") ||
                startScript.contains("nest")) {
                return Execution.ProjectType.SERVER;
            }
        }

        JsonNode dependencies = root.get("dependencies");
        if (dependencies != null) {
            if (dependencies.has("express") ||
                dependencies.has("fastify") ||
                dependencies.has("koa") ||
                dependencies.has("next") ||
                dependencies.has("nuxt") ||
                dependencies.has("@nestjs/core")) {
                return Execution.ProjectType.SERVER;
            }
        }

        return Execution.ProjectType.SCRIPT;
    }

    private Execution.ProjectType detectPythonType(String workingDir) {
        File requirementsTxt = new File(workingDir, "requirements.txt");
        if (requirementsTxt.exists()) {
            try {
                String content = Files.readString(requirementsTxt.toPath());
                if (content.contains("flask") || 
                    content.contains("django") || 
                    content.contains("fastapi") ||
                    content.contains("uvicorn")) {
                    return Execution.ProjectType.SERVER;
                }
            } catch (IOException e) {
                log.warn("Failed to read requirements.txt", e);
            }
        }

        if (new File(workingDir, "app.py").exists() ||
            new File(workingDir, "main.py").exists() ||
            new File(workingDir, "manage.py").exists()) {
            return Execution.ProjectType.SERVER;
        }

        return Execution.ProjectType.SCRIPT;
    }

    private Execution.ProjectType detectJavaType(String workingDir) {
        File pomXml = new File(workingDir, "pom.xml");
        if (pomXml.exists()) {
            try {
                String content = Files.readString(pomXml.toPath());
                if (content.contains("spring-boot-starter-web") ||
                    content.contains("spring-boot-starter-webflux")) {
                    return Execution.ProjectType.SERVER;
                }
            } catch (IOException e) {
                log.warn("Failed to read pom.xml", e);
            }
        }

        File buildGradle = new File(workingDir, "build.gradle");
        if (buildGradle.exists()) {
            try {
                String content = Files.readString(buildGradle.toPath());
                if (content.contains("spring-boot-starter-web")) {
                    return Execution.ProjectType.SERVER;
                }
            } catch (IOException e) {
                log.warn("Failed to read build.gradle", e);
            }
        }

        return Execution.ProjectType.SCRIPT;
    }
}
