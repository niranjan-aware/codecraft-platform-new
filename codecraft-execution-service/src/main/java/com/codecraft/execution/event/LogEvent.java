package com.codecraft.execution.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LogEvent {
    private UUID executionId;
    private String level;
    private String message;
}
