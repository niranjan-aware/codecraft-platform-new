package com.codecraft.project.controller;

import com.codecraft.project.dto.*;
import com.codecraft.project.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/{projectId}/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<FileResponse> createFile(
            @PathVariable UUID projectId,
            @Valid @RequestBody FileRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        FileResponse response = fileService.createFile(userId, projectId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<FileContentResponse> getFile(
            @PathVariable UUID projectId,
            @RequestHeader("X-File-Path") String path,
            @RequestHeader("X-User-Id") String userId) {
        
        FileContentResponse response = fileService.getFileWithContent(projectId, path);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tree")
    public ResponseEntity<FileTreeNode> getFileTree(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId) {
        
        FileTreeNode tree = fileService.getFileTree(projectId);
        return ResponseEntity.ok(tree);
    }

    @PutMapping
    public ResponseEntity<FileResponse> updateFile(
            @PathVariable UUID projectId,
            @RequestHeader("X-File-Path") String path,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody FileRequest request) {
        
        FileResponse response = fileService.updateFile(userId, projectId, path, request.getContent());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID projectId,
            @RequestHeader("X-File-Path") String path,
            @RequestHeader("X-User-Id") String userId) {
        
        fileService.deleteFile(userId, projectId, path);
        return ResponseEntity.noContent().build();
    }
}
