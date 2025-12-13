package com.codecraft.execution.service;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public String downloadProjectFiles(UUID projectId) {
        try {
            String tempDir = "/tmp/codecraft/" + projectId;
            Files.createDirectories(Path.of(tempDir));

            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(projectId.toString() + "/")
                    .recursive(true)
                    .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();
                String relativePath = objectName.substring(projectId.toString().length() + 1);
                
                File localFile = new File(tempDir, relativePath);
                localFile.getParentFile().mkdirs();

                try (GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
                )) {
                    try (FileOutputStream fos = new FileOutputStream(localFile)) {
                        response.transferTo(fos);
                    }
                }

                log.info("Downloaded: {}", relativePath);
            }

            return tempDir;
        } catch (Exception e) {
            log.error("Error downloading project files", e);
            throw new RuntimeException("Failed to download project files", e);
        }
    }

    public void cleanupProjectFiles(UUID projectId) {
        try {
            String tempDir = "/tmp/codecraft/" + projectId;
            Path path = Path.of(tempDir);
            if (Files.exists(path)) {
                Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception e) {
                            log.warn("Failed to delete {}", p);
                        }
                    });
            }
        } catch (Exception e) {
            log.error("Error cleaning up project files", e);
        }
    }
}
