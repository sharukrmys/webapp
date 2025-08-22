package com.example.applib.util;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = true)
public class MinioUtil {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * Upload a file to Minio
     * 
     * @param objectName The object name
     * @param content The file content as byte array
     * @param contentType The content type (MIME type)
     * @param metadata Optional metadata
     * @return The object name
     */
    public String uploadFile(String objectName, byte[] content, String contentType, Map<String, String> metadata) {
        try {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .contentType(contentType)
                    .stream(new ByteArrayInputStream(content), content.length, -1);

            if (metadata != null && !metadata.isEmpty()) {
                builder.userMetadata(metadata);
            }

            minioClient.putObject(builder.build());
            log.info("Successfully uploaded file to Minio: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error uploading file to Minio: {}", objectName, e);
            throw new RuntimeException("Failed to upload file to Minio", e);
        }
    }

    /**
     * Download a file from Minio
     * 
     * @param objectName The object name
     * @return The file content as byte array
     */
    public byte[] downloadFile(String objectName) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();

            try (InputStream stream = minioClient.getObject(getObjectArgs)) {
                return IOUtils.toByteArray(stream);
            }
        } catch (Exception e) {
            log.error("Error downloading file from Minio: {}", objectName, e);
            throw new RuntimeException("Failed to download file from Minio", e);
        }
    }

    /**
     * Stream a file from Minio (memory efficient for large files)
     * 
     * @param objectName The object name
     * @return InputStream for the object
     */
    public InputStream streamFile(String objectName) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();

            return minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.error("Error streaming file from Minio: {}", objectName, e);
            throw new RuntimeException("Failed to stream file from Minio", e);
        }
    }

    /**
     * Delete a file from Minio
     * 
     * @param objectName The object name
     */
    public void deleteFile(String objectName) {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();

            minioClient.removeObject(removeObjectArgs);
            log.info("Successfully deleted file from Minio: {}", objectName);
        } catch (Exception e) {
            log.error("Error deleting file from Minio: {}", objectName, e);
            throw new RuntimeException("Failed to delete file from Minio", e);
        }
    }

    /**
     * List files in a Minio prefix
     * 
     * @param prefix The prefix
     * @return List of object names
     */
    public List<String> listFiles(String prefix) {
        try {
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .recursive(true)
                    .build();

            Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);
            List<String> objectNames = new ArrayList<>();

            for (Result<Item> result : results) {
                Item item = result.get();
                objectNames.add(item.objectName());
            }

            return objectNames;
        } catch (Exception e) {
            log.error("Error listing files from Minio with prefix: {}", prefix, e);
            throw new RuntimeException("Failed to list files from Minio", e);
        }
    }

    /**
     * Generate a pre-signed URL for downloading a file
     * 
     * @param objectName The object name
     * @param expirationInMinutes URL expiration time in minutes
     * @return Pre-signed URL as string
     */
    public String generatePresignedUrl(String objectName, int expirationInMinutes) {
        try {
            GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(expirationInMinutes, TimeUnit.MINUTES)
                    .build();

            return minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
        } catch (Exception e) {
            log.error("Error generating presigned URL for Minio object: {}", objectName, e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    /**
     * Generate a pre-signed URL for uploading a file
     * 
     * @param objectName The object name
     * @param contentType The content type (MIME type)
     * @param expirationInMinutes URL expiration time in minutes
     * @return Pre-signed URL as string
     */
    public String generatePresignedUploadUrl(String objectName, String contentType, int expirationInMinutes) {
        try {
            Map<String, String> reqParams = Map.of("Content-Type", contentType);

            GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .method(Method.PUT)
                    .expiry(expirationInMinutes, TimeUnit.MINUTES)
                    .extraQueryParams(reqParams)
                    .build();

            return minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
        } catch (Exception e) {
            log.error("Error generating presigned upload URL for Minio object: {}", objectName, e);
            throw new RuntimeException("Failed to generate presigned upload URL", e);
        }
    }
}
