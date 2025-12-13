package com.codecraft.project.repository;

import com.codecraft.project.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, UUID> {
    List<ProjectFile> findByProjectId(UUID projectId);
    Optional<ProjectFile> findByProjectIdAndPath(UUID projectId, String path);
    void deleteByProjectId(UUID projectId);
}
