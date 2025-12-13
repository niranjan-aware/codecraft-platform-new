package com.codecraft.project.dto;

import com.codecraft.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String userId;
    private String name;
    private String description;
    private Project.Language language;
    private String framework;  // String, not enum
    private Project.Visibility visibility;
    private String githubUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
