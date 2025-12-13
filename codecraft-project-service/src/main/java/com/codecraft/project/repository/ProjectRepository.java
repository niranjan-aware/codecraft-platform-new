package com.codecraft.project.repository;

import com.codecraft.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    
    List<Project> findByUserId(String userId);  // Back to String!
    
    List<Project> findByVisibility(Project.Visibility visibility);
}
