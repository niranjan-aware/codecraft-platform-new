package com.codecraft.project.service;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                log.info("Bucket created: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Error ensuring bucket exists", e);
            throw new RuntimeException("Failed to create bucket", e);
        }
    }

    public String uploadFile(UUID projectId, String filePath, String content) {
        try {
            ensureBucketExists();
            
            String objectName = projectId + "/" + filePath;
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(contentBytes), contentBytes.length, -1)
                    .contentType("text/plain")
                    .build()
            );

            return objectName;
        } catch (Exception e) {
            log.error("Error uploading file to MinIO", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public String downloadFile(String minioKey) {
        try {
            GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(minioKey)
                    .build()
            );

            return new String(response.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error downloading file from MinIO", e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public void deleteFile(String minioKey) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(minioKey)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error deleting file from MinIO", e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public void deleteFolder(UUID projectId) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(projectId.toString() + "/")
                    .recursive(true)
                    .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                deleteFile(item.objectName());
            }
        } catch (Exception e) {
            log.error("Error deleting folder from MinIO", e);
            throw new RuntimeException("Failed to delete folder", e);
        }
    }
}
