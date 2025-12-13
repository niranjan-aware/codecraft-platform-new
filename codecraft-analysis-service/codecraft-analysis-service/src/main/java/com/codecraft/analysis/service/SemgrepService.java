package com.codecraft.analysis.service;

import com.codecraft.analysis.entity.CodeIssue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemgrepService {

    private final ObjectMapper objectMapper;

    public List<CodeIssue> runSemgrep(UUID reportId, String projectPath, String language) {
        List<CodeIssue> issues = new ArrayList<>();
        
        try {
            String config = getSemgrepConfig(language);
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                "semgrep",
                "--config", config,
                "--json",
                "--quiet",
                projectPath
            );
            
            processBuilder.directory(new File(projectPath));
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            
            if (exitCode == 0 || exitCode == 1) {
                JsonNode results = objectMapper.readTree(output.toString());
                JsonNode resultsArray = results.get("results");
                
                if (resultsArray != null && resultsArray.isArray()) {
                    for (JsonNode result : resultsArray) {
                        CodeIssue issue = parseSecurityIssue(reportId, result);
                        if (issue != null) {
                            issues.add(issue);
                        }
                    }
                }
            } else {
                log.error("Semgrep failed with exit code: {}", exitCode);
            }

        } catch (Exception e) {
            log.error("Error running Semgrep", e);
        }

        return issues;
    }

    private CodeIssue parseSecurityIssue(UUID reportId, JsonNode result) {
        try {
            CodeIssue issue = new CodeIssue();
            issue.setAnalysisReportId(reportId);
            
            String path = result.get("path").asText();
            issue.setFilePath(path);
            
            JsonNode start = result.get("start");
            if (start != null) {
                issue.setLineNumber(start.get("line").asInt());
            }
            
            JsonNode extra = result.get("extra");
            if (extra != null) {
                String severity = extra.get("severity").asText();
                issue.setSeverity(mapSeverity(severity));
                
                String message = extra.get("message").asText();
                issue.setMessage(message);
                
                JsonNode metadata = extra.get("metadata");
                if (metadata != null) {
                    issue.setRuleId(metadata.has("category") ? 
                        metadata.get("category").asText() : "semgrep-rule");
                }
            }
            
            JsonNode extraNode = result.get("extra");
            if (extraNode != null && extraNode.has("lines")) {
                issue.setCodeSnippet(extraNode.get("lines").asText());
            }
            
            issue.setAiFixAvailable(false);
            
            return issue;
        } catch (Exception e) {
            log.error("Error parsing Semgrep result", e);
            return null;
        }
    }

    private CodeIssue.Severity mapSeverity(String severity) {
        return switch (severity.toUpperCase()) {
            case "ERROR" -> CodeIssue.Severity.CRITICAL;
            case "WARNING" -> CodeIssue.Severity.MAJOR;
            case "INFO" -> CodeIssue.Severity.MINOR;
            default -> CodeIssue.Severity.INFO;
        };
    }

    private String getSemgrepConfig(String language) {
        return switch (language.toUpperCase()) {
            case "NODEJS", "JAVASCRIPT" -> "p/javascript";
            case "PYTHON" -> "p/python";
            case "JAVA" -> "p/java";
            default -> "p/security-audit";
        };
    }
}
