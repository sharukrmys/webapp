package com.example.applib.service;

import com.example.applib.entity.TurboS3Config;
import com.example.applib.repository.TurboS3ConfigRepository;
import io.minio.MinioClient;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final TurboS3ConfigRepository turboS3ConfigRepository;

    @Value("${aws.s3.enabled:true}")
    private boolean awsS3Enabled;

    @Value("${minio.enabled:false}")
    private boolean minioEnabled;

    public List<TurboS3Config> getAllConfigs() {
        return turboS3ConfigRepository.findAll();
    }

    public Optional<TurboS3Config> getConfigById(Long id) {
        return turboS3ConfigRepository.findById(id);
    }

    public TurboS3Config createConfig(TurboS3Config config) {
        return turboS3ConfigRepository.save(config);
    }

    public Optional<TurboS3Config> updateConfig(Long id, TurboS3Config config) {
        return turboS3ConfigRepository.findById(id)
                .map(existingConfig -> {
                    config.setId(id);
                    return turboS3ConfigRepository.save(config);
                });
    }

    public boolean deleteConfig(Long id) {
        return turboS3ConfigRepository.findById(id)
                .map(config -> {
                    turboS3ConfigRepository.delete(config);
                    return true;
                })
                .orElse(false);
    }

    @Configuration
    static class S3Configuration {

        @Bean
        @Primary
        @ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
        public S3Client awsS3Client(TurboS3ConfigRepository repository) {
            TurboS3Config config = repository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No S3 configuration found"));

            return S3Client.builder()
                    .region(Region.of(config.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(
                                    config.getAwsAccessKeyId(),
                                    config.getAwsSecretAccessKey()
                            )
                    ))
                    .build();
        }

        @Bean
        @Profile("local")
        @ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
        public MinioClient minioClient(
                @Value("${minio.endpoint}") String endpoint,
                @Value("${minio.access-key}") String accessKey,
                @Value("${minio.secret-key}") String secretKey) {

            return MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
    }
}
