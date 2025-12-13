package com.codecraft.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
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
    private String name;

    @Column(nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    private DependencyType dependencyType = DependencyType.DIRECT;

    @Column(length = 5000)
    private String vulnerabilitiesJson;

    private String license;

    private String latestVersion;

    private Boolean isOutdated = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum DependencyType {
        DIRECT, TRANSITIVE
    }
}
