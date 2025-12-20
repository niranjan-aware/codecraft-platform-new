package com.codecraft.execution.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EnvironmentParser {

    /**
     * Parse .env file and extract variables
     */
    public Map<String, String> parseEnvFile(String workingDir, String envFileName) {
        Map<String, String> envVars = new HashMap<>();
        
        File envFile = new File(workingDir, envFileName);
        if (!envFile.exists()) {
            log.debug("No {} file found in {}", envFileName, workingDir);
            return envVars;
        }

        try {
            List<String> lines = Files.readAllLines(envFile.toPath());
            
            for (String line : lines) {
                line = line.trim();
                
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse KEY=VALUE format
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    
                    // Remove quotes if present
                    value = value.replaceAll("^\"|\"$", "");
                    value = value.replaceAll("^'|'$", "");
                    
                    envVars.put(key, value);
                }
            }
            
            log.info("Parsed {} variables from {}", envVars.size(), envFileName);
        } catch (Exception e) {
            log.error("Failed to parse .env file", e);
        }
        
        return envVars;
    }

    /**
     * Get PORT from .env file with fallback to default
     */
    public Integer getPort(String workingDir, Integer defaultPort) {
        // Try .env first
        Map<String, String> envVars = parseEnvFile(workingDir, ".env");
        
        if (envVars.containsKey("PORT")) {
            try {
                return Integer.parseInt(envVars.get("PORT"));
            } catch (NumberFormatException e) {
                log.warn("Invalid PORT value in .env: {}", envVars.get("PORT"));
            }
        }
        
        // Try .env.production
        envVars = parseEnvFile(workingDir, ".env.production");
        if (envVars.containsKey("PORT")) {
            try {
                return Integer.parseInt(envVars.get("PORT"));
            } catch (NumberFormatException e) {
                log.warn("Invalid PORT value in .env.production: {}", envVars.get("PORT"));
            }
        }
        
        return defaultPort;
    }

    /**
     * Get default port for language/framework
     */
    public Integer getDefaultPort(String language) {
        return switch (language.toUpperCase()) {
            case "NODEJS" -> 3000;
            case "PYTHON" -> 5000;
            case "JAVA" -> 8080;
            case "HTML_CSS_JS" -> 8080;
            default -> 3000;
        };
    }

    /**
     * Merge .env files with priority: .env > .env.production > .env.development
     */
    public Map<String, String> getAllEnvVars(String workingDir) {
        Map<String, String> allVars = new HashMap<>();
        
        // Load in reverse priority order
        allVars.putAll(parseEnvFile(workingDir, ".env.development"));
        allVars.putAll(parseEnvFile(workingDir, ".env.production"));
        allVars.putAll(parseEnvFile(workingDir, ".env"));
        
        return allVars;
    }
}
