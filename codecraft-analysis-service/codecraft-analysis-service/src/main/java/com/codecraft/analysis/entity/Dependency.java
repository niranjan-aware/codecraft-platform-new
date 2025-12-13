package com.codecraft.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "dependencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dependency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private UUID analysisReportId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    private DependencyType dependencyType;

    @Column(length = 10000)
    private String vulnerabilities;

    private String license;

    private Boolean outdated = false;

    private String latestVersion;

    public enum DependencyType {
        DIRECT, TRANSITIVE
    }
}
