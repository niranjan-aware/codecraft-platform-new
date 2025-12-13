package com.codecraft.project.dto;

import com.codecraft.project.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    
    @NotBlank(message = "Project name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Language is required")
    private Project.Language language;
    
    private String framework;  // String, not enum
    
    private Project.Visibility visibility = Project.Visibility.PRIVATE;
}
