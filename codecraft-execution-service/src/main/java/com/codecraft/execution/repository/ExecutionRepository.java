package com.codecraft.execution.repository;

import com.codecraft.execution.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
    List<Execution> findByProjectId(UUID projectId);
    List<Execution> findByUserId(UUID userId);
    Optional<Execution> findByContainerId(String containerId);
    List<Execution> findByStatus(Execution.ExecutionStatus status);
}
