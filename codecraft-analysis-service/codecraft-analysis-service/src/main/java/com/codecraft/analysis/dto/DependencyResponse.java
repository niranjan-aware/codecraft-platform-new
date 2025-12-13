package com.codecraft.analysis.dto;

import com.codecraft.analysis.entity.Dependency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DependencyResponse {
    private UUID id;
    private String name;
    private String version;
    private Dependency.DependencyType dependencyType;
    private String vulnerabilities;
    private String license;
    private Boolean outdated;
    private String latestVersion;
}
