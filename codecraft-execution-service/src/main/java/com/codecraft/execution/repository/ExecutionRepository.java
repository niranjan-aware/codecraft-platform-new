package com.codecraft.execution.repository;

import com.codecraft.execution.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
    List<Execution> findByProjectId(UUID projectId);
    List<Execution> findByUserId(UUID userId);
    List<Execution> findByStatus(Execution.ExecutionStatus status);
    List<Execution> findByStatusAndAutoStopAtBefore(Execution.ExecutionStatus status, LocalDateTime dateTime);
}
