package com.codecraft.project.service;

import com.codecraft.project.dto.CreateProjectRequest;
import com.codecraft.project.dto.ProjectResponse;
import com.codecraft.project.entity.Project;
import com.codecraft.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID userId) {
        Project project = new Project();
        project.setUserId(userId.toString());  // Back to toString()
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLanguage(request.getLanguage());
        project.setFramework(request.getFramework());
        project.setVisibility(request.getVisibility());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    public List<ProjectResponse> listProjects(UUID userId) {
        return projectRepository.findByUserId(userId.toString())  // Back to toString()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId.toString()) && project.getVisibility() != Project.Visibility.PUBLIC) {
            throw new RuntimeException("Unauthorized");
        }

        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, CreateProjectRequest request, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId.toString())) {
            throw new RuntimeException("Unauthorized");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setVisibility(request.getVisibility());
        project.setUpdatedAt(LocalDateTime.now());

        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId.toString())) {
            throw new RuntimeException("Unauthorized");
        }

        projectRepository.delete(project);
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .userId(project.getUserId())  // Already String
                .name(project.getName())
                .description(project.getDescription())
                .language(project.getLanguage())
                .framework(project.getFramework())
                .visibility(project.getVisibility())
                .githubUrl(project.getGithubUrl())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
