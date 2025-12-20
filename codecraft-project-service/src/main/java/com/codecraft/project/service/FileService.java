package com.codecraft.project.service;

import com.codecraft.project.dto.*;
import com.codecraft.project.entity.Project;
import com.codecraft.project.entity.ProjectFile;
import com.codecraft.project.repository.ProjectFileRepository;
import com.codecraft.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final ProjectFileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final StorageService storageService;

    @Transactional
    public FileResponse createFile(UUID projectId, FileRequest request, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        log.info("Creating file - Project userId: {}, Request userId: {}", project.getUserId(), userId);

        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        Optional<ProjectFile> existing = fileRepository.findByProjectIdAndPath(projectId, request.getPath());
        if (existing.isPresent()) {
            throw new RuntimeException("File already exists");
        }

        String minioKey = storageService.uploadFile(projectId, request.getPath(), request.getContent());

        ProjectFile file = new ProjectFile();
        file.setProjectId(projectId);
        file.setPath(request.getPath());
        file.setMinioKey(minioKey);
        file.setSizeBytes((long) request.getContent().getBytes().length);
        file.setMimeType(detectMimeType(request.getPath()));

        file = fileRepository.save(file);

        return mapToResponse(file);
    }

    @Transactional
    public FileResponse updateFile(UUID projectId, String path, FileRequest request, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        ProjectFile file = fileRepository.findByProjectIdAndPath(projectId, path)
                .orElseThrow(() -> new RuntimeException("File not found"));

        String minioKey = storageService.uploadFile(projectId, request.getPath(), request.getContent());

        file.setMinioKey(minioKey);
        file.setSizeBytes((long) request.getContent().getBytes().length);

        file = fileRepository.save(file);

        return mapToResponse(file);
    }

    public FileContentResponse getFile(UUID projectId, String path, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId) && project.getVisibility() == Project.Visibility.PRIVATE) {
            throw new RuntimeException("Unauthorized");
        }

        ProjectFile file = fileRepository.findByProjectIdAndPath(projectId, path)
                .orElseThrow(() -> new RuntimeException("File not found"));

        String content = storageService.downloadFile(file.getMinioKey());

        return new FileContentResponse(mapToResponse(file), content);
    }

    public List<FileResponse> listFiles(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId) && project.getVisibility() == Project.Visibility.PRIVATE) {
            throw new RuntimeException("Unauthorized");
        }

        return fileRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FileTreeNode getFileTree(UUID projectId, UUID userId) {
        List<FileResponse> files = listFiles(projectId, userId);
        return buildFileTree(files);
    }

    @Transactional
    public void deleteFile(UUID projectId, String path, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        ProjectFile file = fileRepository.findByProjectIdAndPath(projectId, path)
                .orElseThrow(() -> new RuntimeException("File not found"));

        storageService.deleteFile(file.getMinioKey());
        fileRepository.delete(file);
    }

    private FileTreeNode buildFileTree(List<FileResponse> files) {
        FileTreeNode root = new FileTreeNode();
        root.setName("root");
        root.setPath("/");
        root.setType("directory");

        for (FileResponse file : files) {
            String[] parts = file.getPath().split("/");
            FileTreeNode current = root;

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (part.isEmpty()) continue;

                boolean isFile = (i == parts.length - 1);
                String currentPath = String.join("/", Arrays.copyOfRange(parts, 0, i + 1));

                FileTreeNode child = current.getChildren().stream()
                        .filter(n -> n.getName().equals(part))
                        .findFirst()
                        .orElse(null);

                if (child == null) {
                    child = new FileTreeNode();
                    child.setName(part);
                    child.setPath(currentPath);
                    child.setType(isFile ? "file" : "directory");
                    current.getChildren().add(child);
                }

                current = child;
            }
        }

        return root;
    }

    private String detectMimeType(String path) {
        if (path.endsWith(".js")) return "text/javascript";
        if (path.endsWith(".jsx")) return "text/javascript";
        if (path.endsWith(".ts")) return "text/typescript";
        if (path.endsWith(".tsx")) return "text/typescript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".py")) return "text/x-python";
        if (path.endsWith(".java")) return "text/x-java";
        return "text/plain";
    }

    private FileResponse mapToResponse(ProjectFile file) {
        return new FileResponse(
                file.getId(),
                file.getProjectId(),
                file.getPath(),
                file.getSizeBytes(),
                file.getMimeType(),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }
}
