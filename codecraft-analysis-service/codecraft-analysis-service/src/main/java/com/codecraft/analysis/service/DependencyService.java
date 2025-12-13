package com.codecraft.analysis.service;

import com.codecraft.analysis.entity.Dependency;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DependencyService {

    private final ObjectMapper objectMapper;

    public List<Dependency> analyzeDependencies(UUID projectId, UUID reportId, String projectPath, String language) {
        List<Dependency> dependencies = new ArrayList<>();

        try {
            switch (language.toUpperCase()) {
                case "NODEJS" -> dependencies = analyzeNodeDependencies(projectId, reportId, projectPath);
                case "PYTHON" -> dependencies = analyzePythonDependencies(projectId, reportId, projectPath);
                case "JAVA" -> dependencies = analyzeJavaDependencies(projectId, reportId, projectPath);
            }
        } catch (Exception e) {
            log.error("Error analyzing dependencies", e);
        }

        return dependencies;
    }

    private List<Dependency> analyzeNodeDependencies(UUID projectId, UUID reportId, String projectPath) {
        List<Dependency> dependencies = new ArrayList<>();
        
        try {
            File packageJson = new File(projectPath, "package.json");
            if (!packageJson.exists()) {
                return dependencies;
            }

            String content = Files.readString(packageJson.toPath());
            JsonNode root = objectMapper.readTree(content);
            
            JsonNode depsNode = root.get("dependencies");
            if (depsNode != null) {
                depsNode.fields().forEachRemaining(entry -> {
                    Dependency dep = new Dependency();
                    dep.setProjectId(projectId);
                    dep.setAnalysisReportId(reportId);
                    dep.setName(entry.getKey());
                    dep.setVersion(entry.getValue().asText().replace("^", "").replace("~", ""));
                    dep.setDependencyType(Dependency.DependencyType.DIRECT);
                    dep.setOutdated(false);
                    dependencies.add(dep);
                });
            }

            JsonNode devDepsNode = root.get("devDependencies");
            if (devDepsNode != null) {
                devDepsNode.fields().forEachRemaining(entry -> {
                    Dependency dep = new Dependency();
                    dep.setProjectId(projectId);
                    dep.setAnalysisReportId(reportId);
                    dep.setName(entry.getKey());
                    dep.setVersion(entry.getValue().asText().replace("^", "").replace("~", ""));
                    dep.setDependencyType(Dependency.DependencyType.DIRECT);
                    dep.setOutdated(false);
                    dependencies.add(dep);
                });
            }

        } catch (Exception e) {
            log.error("Error analyzing Node.js dependencies", e);
        }

        return dependencies;
    }

    private List<Dependency> analyzePythonDependencies(UUID projectId, UUID reportId, String projectPath) {
        List<Dependency> dependencies = new ArrayList<>();
        
        try {
            File requirementsTxt = new File(projectPath, "requirements.txt");
            if (!requirementsTxt.exists()) {
                return dependencies;
            }

            List<String> lines = Files.readAllLines(requirementsTxt.toPath());
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("==|>=|<=|~=");
                if (parts.length > 0) {
                    Dependency dep = new Dependency();
                    dep.setProjectId(projectId);
                    dep.setAnalysisReportId(reportId);
                    dep.setName(parts[0].trim());
                    dep.setVersion(parts.length > 1 ? parts[1].trim() : "latest");
                    dep.setDependencyType(Dependency.DependencyType.DIRECT);
                    dep.setOutdated(false);
                    dependencies.add(dep);
                }
            }

        } catch (Exception e) {
            log.error("Error analyzing Python dependencies", e);
        }

        return dependencies;
    }

    private List<Dependency> analyzeJavaDependencies(UUID projectId, UUID reportId, String projectPath) {
        List<Dependency> dependencies = new ArrayList<>();
        
        try {
            File pomXml = new File(projectPath, "pom.xml");
            if (!pomXml.exists()) {
                return dependencies;
            }

        } catch (Exception e) {
            log.error("Error analyzing Java dependencies", e);
        }

        return dependencies;
    }
}
