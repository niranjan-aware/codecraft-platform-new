package com.codecraft.project.service;

import com.codecraft.project.dto.CreateProjectRequest;
import com.codecraft.project.dto.ProjectResponse;
import com.codecraft.project.entity.Project;
import com.codecraft.project.repository.ProjectRepository;
import com.codecraft.project.repository.ProjectFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository fileRepository;
    private final StorageService storageService;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID userId) {
        Project project = new Project();
        project.setUserId(userId);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLanguage(request.getLanguage());
        project.setFramework(request.getFramework());
        project.setVisibility(request.getVisibility());
        project.setProjectType(request.getProjectType());

        project = projectRepository.save(project);

        initializeProjectFiles(project);

        return mapToResponse(project);
    }

    public ProjectResponse getProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId) && project.getVisibility() == Project.Visibility.PRIVATE) {
            throw new RuntimeException("Unauthorized");
        }

        return mapToResponse(project);
    }

    public List<ProjectResponse> listProjects(UUID userId) {
        return projectRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, CreateProjectRequest request, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setVisibility(request.getVisibility());
        project.setProjectType(request.getProjectType());

        project = projectRepository.save(project);

        return mapToResponse(project);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        storageService.deleteFolder(projectId);
        fileRepository.deleteByProjectId(projectId);
        projectRepository.delete(project);
    }

    private void initializeProjectFiles(Project project) {
        String template = getTemplateContent(project.getLanguage(), project.getFramework());
        storageService.uploadFile(project.getId(), "README.md", template);
    }

    private String getTemplateContent(Project.Language language, Project.Framework framework) {
        return String.format("# %s Project\n\nProject created with CodeCraft\n\nLanguage: %s\nFramework: %s",
                language, language, framework != null ? framework : "None");
    }

    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getUserId(),
                project.getName(),
                project.getDescription(),
                project.getLanguage(),
                project.getFramework(),
                project.getVisibility(),
                project.getProjectType(),
                project.getGithubUrl(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
