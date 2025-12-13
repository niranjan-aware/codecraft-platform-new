package com.codecraft.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileRequest {
    
    @NotBlank(message = "File path is required")
    private String path;
    
    @NotBlank(message = "File content is required")
    private String content;
}
