package com.codecraft.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class FileResponse {
    private UUID id;
    private UUID projectId;
    private String path;
    private Long sizeBytes;
    private String mimeType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
