package com.codecraft.project.controller;

import com.codecraft.project.dto.CreateProjectRequest;
import com.codecraft.project.dto.ProjectResponse;
import com.codecraft.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @RequestHeader("X-User-Id") String userId) {
        ProjectResponse response = projectService.createProject(request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects(
            @RequestHeader("X-User-Id") String userId) {
        List<ProjectResponse> projects = projectService.listProjects(UUID.fromString(userId));
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId) {
        ProjectResponse response = projectService.getProject(projectId, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateProjectRequest request,
            @RequestHeader("X-User-Id") String userId) {
        ProjectResponse response = projectService.updateProject(projectId, request, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") String userId) {
        projectService.deleteProject(projectId, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Project Service is running");
    }
}
