package com.codecraft.project.dto;

import com.codecraft.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String description;
    private Project.Language language;
    private Project.Framework framework;
    private Project.Visibility visibility;
    private String githubUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
