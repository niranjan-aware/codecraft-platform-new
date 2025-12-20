package com.codecraft.project.dto;

import com.codecraft.project.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProjectRequest {
    
    @NotBlank(message = "Project name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Language is required")
    private Project.Language language;
    
    private Project.Framework framework;
    
    private Project.Visibility visibility = Project.Visibility.PRIVATE;
    
    private Project.ProjectType projectType;
}
