package com.codecraft.execution.event;

import com.codecraft.execution.dto.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    @Async("websocketTaskExecutor")  // Use dedicated executor
    public void handleLogEvent(LogEvent event) {
        log.info("📨 Publishing log to WebSocket from thread: {}", Thread.currentThread().getName());
        
        LogMessage logMessage = new LogMessage(
            event.getLevel(), 
            event.getMessage(), 
            LocalDateTime.now()
        );
        
        try {
            messagingTemplate.convertAndSend(
                "/topic/execution/" + event.getExecutionId(), 
                logMessage
            );
            log.info("✅ Log published successfully");
        } catch (Exception e) {
            log.error("❌ Failed to publish log", e);
        }
    }
}
