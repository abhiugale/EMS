package com.ems.infrastructure.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioService {
    private static final Logger log = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;

    @Value("${minio.bucket:ems-bucket}")
    private String defaultBucket;

    public MinioService(
            @Value("${minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${minio.accessKey:minioadmin}") String accessKey,
            @Value("${minio.secretKey:minioadmin}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public void uploadFile(String objectName, InputStream inputStream, String contentType, long size) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(defaultBucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(defaultBucket).build());
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("Successfully uploaded object to MinIO: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO object={}. Error: {}", objectName, e.getMessage());
            throw new RuntimeException("MinIO storage failed: " + e.getMessage(), e);
        }
    }

    public String getDefaultBucket() {
        return defaultBucket;
    }
}
