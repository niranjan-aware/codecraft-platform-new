package com.codecraft.project.controller;

import com.codecraft.project.dto.*;
import com.codecraft.project.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/{projectId}/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<FileResponse> createFile(
            @PathVariable UUID projectId,
            @Valid @RequestBody FileRequest request,
            @RequestHeader("X-User-Id") String userId) {
        FileResponse response = fileService.createFile(projectId, request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> listFiles(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId) {
        List<FileResponse> files = fileService.listFiles(projectId, UUID.fromString(userId));
        return ResponseEntity.ok(files);
    }

    @GetMapping("/tree")
    public ResponseEntity<FileTreeNode> getFileTree(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId) {
        FileTreeNode tree = fileService.getFileTree(projectId, UUID.fromString(userId));
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/**")
    public ResponseEntity<FileContentResponse> getFile(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-File-Path") String filePath) {
        FileContentResponse response = fileService.getFile(projectId, filePath, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/**")
    public ResponseEntity<FileResponse> updateFile(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-File-Path") String filePath,
            @Valid @RequestBody FileRequest request) {
        FileResponse response = fileService.updateFile(projectId, filePath, request, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/**")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-File-Path") String filePath) {
        fileService.deleteFile(projectId, filePath, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
