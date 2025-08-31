package com.example.applib.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Minio.
 * This is conditionally enabled based on the minio.enabled property.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
public class MinioConfig {

    private final FeatureToggleConfig.MinioFeatureProperties minioProperties;

    /**
     * Creates a Minio client for Minio operations.
     */
    @Bean
    public MinioClient minioClient() {
        log.info("Configuring Minio client with endpoint: {}", minioProperties.getEndpoint());
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}
