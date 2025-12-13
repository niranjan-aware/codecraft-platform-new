package com.codecraft.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileContentResponse {
    private FileResponse file;
    private String content;
}
