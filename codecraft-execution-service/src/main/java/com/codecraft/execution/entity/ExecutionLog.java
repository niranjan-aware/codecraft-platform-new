package com.codecraft.execution.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "execution_logs", indexes = {
    @Index(name = "idx_execution_id", columnList = "execution_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID executionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogLevel logLevel;

    @Column(nullable = false, length = 10000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public enum LogLevel {
        INFO, ERROR, WARN, DEBUG
    }
}
