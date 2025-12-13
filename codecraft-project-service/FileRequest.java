package com.codecraft.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileRequest {
    
    @NotBlank(message = "File path is required")
    private String path;
    
    // Changed from @NotBlank to allow empty content for new files
    private String content = "";
}
