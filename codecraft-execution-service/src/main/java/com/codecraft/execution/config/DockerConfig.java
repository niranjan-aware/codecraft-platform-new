package com.codecraft.execution.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DockerConfig {

    @Bean
    public DockerClient dockerClient() {
        String dockerHost = "unix:///var/run/docker.sock";
        
        log.info("=== CREATING DOCKER CLIENT (Zerodep Transport) ===");
        log.info("Docker Host: {}", dockerHost);
        
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        log.info("Config Docker Host: {}", config.getDockerHost());

        DockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();

        DockerClient client = DockerClientImpl.getInstance(config, httpClient);
        
        try {
            log.info("Testing Docker connection...");
            client.pingCmd().exec();
            log.info("✅ Docker PING successful!");
            
            var version = client.versionCmd().exec();
            log.info("✅ Docker Version: {}", version.getVersion());
            log.info("✅ API Version: {}", version.getApiVersion());
        } catch (Exception e) {
            log.error("❌ Docker connection failed!", e);
            throw new RuntimeException("Cannot connect to Docker", e);
        }
        
        return client;
    }
}
