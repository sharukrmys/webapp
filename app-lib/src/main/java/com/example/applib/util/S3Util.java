package com.example.applib.util;

import com.example.applib.entity.TurboS3Config;
import com.example.applib.repository.TurboS3ConfigRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Util {

    private final TurboS3ConfigRepository s3ConfigRepository;

    // Cache S3 clients by configuration ID
    private final Map<Long, S3Client> s3ClientCache = new ConcurrentHashMap<>();

    // Cache S3 presigners by configuration ID
    private final Map<Long, S3Presigner> s3PresignerCache = new ConcurrentHashMap<>();

    /**
     * Get the S3 configuration
     *
     * @return S3 configuration
     */
    private TurboS3Config getS3Config() {
        List<TurboS3Config> configs = s3ConfigRepository.findAll();
        if (configs.isEmpty()) {
            log.warn("No S3 configuration found");
            return null;
        }
        return configs.get(0);
    }

    /**
     * Get an S3 client for the current configuration
     *
     * @return S3 client
     */
    private S3Client getS3Client() {
        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            log.warn("No S3 configuration found");
            return null;
        }

        // Check cache first
        if (s3ClientCache.containsKey(s3Config.getId())) {
            return s3ClientCache.get(s3Config.getId());
        }

        try {
            // Create AWS credentials
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    s3Config.getAwsAccessKeyId(),
                    s3Config.getAwsSecretAccessKey()
            );

            // Create S3 client
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(s3Config.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            // Cache the client
            s3ClientCache.put(s3Config.getId(), s3Client);

            return s3Client;
        } catch (Exception e) {
            log.error("Error creating S3 client", e);
            return null;
        }
    }

    /**
     * Get an S3 presigner for the current configuration
     *
     * @return S3 presigner
     */
    private S3Presigner getS3Presigner() {
        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            log.warn("No S3 configuration found");
            return null;
        }

        // Check cache first
        if (s3PresignerCache.containsKey(s3Config.getId())) {
            return s3PresignerCache.get(s3Config.getId());
        }

        try {
            // Create AWS credentials
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    s3Config.getAwsAccessKeyId(),
                    s3Config.getAwsSecretAccessKey()
            );

            // Create S3 presigner
            S3Presigner s3Presigner = S3Presigner.builder()
                    .region(Region.of(s3Config.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            // Cache the presigner
            s3PresignerCache.put(s3Config.getId(), s3Presigner);

            return s3Presigner;
        } catch (Exception e) {
            log.error("Error creating S3 presigner", e);
            return null;
        }
    }

    /**
     * Download a file from S3
     *
     * @param keyName S3 object key
     * @param filePath Local file path to save the downloaded file
     * @throws IOException If an I/O error occurs
     */
    public void downloadFile(String keyName, String filePath) throws IOException {
        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            throw new IOException("No S3 configuration found");
        }

        S3Client s3Client = getS3Client();
        if (s3Client == null) {
            throw new IOException("Failed to create S3 client");
        }

        try {
            log.info("Downloading object from S3: {} to local file: {}", keyName, filePath);

            // Create GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(keyName)
                    .build();

            // Get object from S3
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            // Save the file
            saveFile(s3Object, filePath);

            log.info("File downloaded successfully");
        } catch (S3Exception e) {
            log.error("Error downloading file from S3", e);
            throw new IOException("Error downloading file from S3: " + e.getMessage(), e);
        }
    }

    /**
     * Upload a file to S3
     *
     * @param filePath Local file path to upload
     * @param keyName S3 object key
     * @throws IOException If an I/O error occurs
     */
    public void uploadFile(String filePath, String keyName) throws IOException {
        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            throw new IOException("No S3 configuration found");
        }

        S3Client s3Client = getS3Client();
        if (s3Client == null) {
            throw new IOException("Failed to create S3 client");
        }

        try {
            log.info("Uploading file to S3: {} with key: {}", filePath, keyName);

            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(keyName)
                    .build();

            // Upload file to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(filePath)));

            log.info("File uploaded successfully");
        } catch (S3Exception e) {
            log.error("Error uploading file to S3", e);
            throw new IOException("Error uploading file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a pre-signed URL for downloading an object from S3
     *
     * @param keyName S3 object key
     * @param expirationMinutes URL expiration time in minutes
     * @return Pre-signed URL
     */
    public URL generatePresignedDownloadUrl(String keyName, int expirationMinutes) {
        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            log.warn("No S3 configuration found");
            return null;
        }

        S3Presigner presigner = getS3Presigner();
        if (presigner == null) {
            log.warn("Failed to create S3 presigner");
            return null;
        }

        try {
            // Create GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(keyName)
                    .build();

            // Create GetObjectPresignRequest
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            // Generate pre-signed URL
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

            return presignedRequest.url();
        } catch (Exception e) {
            log.error("Error generating pre-signed download URL", e);
            return null;
        }
    }

    /**
     * Generate a pre-signed URL for uploading an object to S3
     *
     * @param keyName S3 object key
     * @param expirationMinutes URL expiration time in minutes
     * @return Pre-signed URL
     */
    public URL generatePresignedUploadUrl(String keyName, int expirationMinutes) {
        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            log.warn("No S3 configuration found");
            return null;
        }

        S3Presigner presigner = getS3Presigner();
        if (presigner == null) {
            log.warn("Failed to create S3 presigner");
            return null;
        }

        try {
            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(keyName)
                    .build();

            // Create PutObjectPresignRequest
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .putObjectRequest(putObjectRequest)
                    .build();

            // Generate pre-signed URL
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            return presignedRequest.url();
        } catch (Exception e) {
            log.error("Error generating pre-signed upload URL", e);
            return null;
        }
    }

    /**
     * Check if an object exists in S3
     *
     * @param keyName S3 object key
     * @return true if the object exists, false otherwise
     */
    public boolean doesObjectExist(String keyName) {
        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            log.warn("No S3 configuration found");
            return false;
        }

        S3Client s3Client = getS3Client();
        if (s3Client == null) {
            log.warn("Failed to create S3 client");
            return false;
        }

        try {
            // Create HeadObjectRequest
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(keyName)
                    .build();

            // Check if object exists
            s3Client.headObject(headObjectRequest);

            return true;
        } catch (NoSuchKeyException e) {
            // Object does not exist
            return false;
        } catch (Exception e) {
            log.error("Error checking if object exists in S3", e);
            return false;
        }
    }

    /**
     * Delete an object from S3
     *
     * @param keyName S3 object key
     * @return true if the object was deleted, false otherwise
     */
    public boolean deleteObject(String keyName) {
        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            log.warn("No S3 configuration found");
            return false;
        }

        S3Client s3Client = getS3Client();
        if (s3Client == null) {
            log.warn("Failed to create S3 client");
            return false;
        }

        try {
            // Create DeleteObjectRequest
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(keyName)
                    .build();

            // Delete object
            s3Client.deleteObject(deleteObjectRequest);

            return true;
        } catch (Exception e) {
            log.error("Error deleting object from S3", e);
            return false;
        }
    }

    /**
     * Save an input stream to a file
     *
     * @param inputStream Input stream to save
     * @param filePath Local file path to save the input stream
     * @throws IOException If an I/O error occurs
     */
    private void saveFile(InputStream inputStream, String filePath) throws IOException {
        File file = new File(filePath);

        // Create parent directories if they don't exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Save the file
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            inputStream.close();
        }
    }
}
