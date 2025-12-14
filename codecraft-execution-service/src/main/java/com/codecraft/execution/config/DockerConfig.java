package com.codecraft.execution.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Properties;

@Slf4j
@Configuration
public class DockerConfig {

    @Bean
    public DockerClient dockerClient() {
        String dockerHost = "unix:///var/run/docker.sock";
        log.info("=== CREATING DOCKER CLIENT WITH HOST: {} ===", dockerHost);
        
        Properties props = new Properties();
        props.setProperty("DOCKER_HOST", dockerHost);
        
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withProperties(props)
                .build();

        log.info("=== FINAL DOCKER HOST: {} ===", config.getDockerHost());

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        DockerClient client = DockerClientImpl.getInstance(config, httpClient);
        log.info("=== DOCKER CLIENT CREATED ===");
        return client;
    }
}
