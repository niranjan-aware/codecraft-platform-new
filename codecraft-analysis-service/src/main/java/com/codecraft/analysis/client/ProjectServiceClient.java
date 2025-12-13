package com.codecraft.analysis.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "project-service")
public interface ProjectServiceClient {

    @GetMapping("/{projectId}")
    Map<String, Object> getProject(
        @PathVariable UUID projectId,
        @RequestHeader("X-User-Id") String userId
    );

    @GetMapping("/{projectId}/files")
    List<Map<String, Object>> getProjectFiles(
        @PathVariable UUID projectId,
        @RequestHeader("X-User-Id") String userId
    );
}
