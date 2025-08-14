package com.example.applib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true", matchIfMissing = true)
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;
    
    @Value("${aws.endpoint-override:#{null}}")
    private String endpointOverride;

    @Bean
    public S3Client s3Client() {
        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            return S3Client.builder()
                    .region(Region.of(region))
                    .endpointOverride(URI.create(endpointOverride))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
        
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public SqsClient sqsClient() {
        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            return SqsClient.builder()
                    .region(Region.of(region))
                    .endpointOverride(URI.create(endpointOverride))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
        
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "aws.secretsmanager.enabled", havingValue = "true")
    public SecretsManagerClient secretsManagerClient() {
        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            return SecretsManagerClient.builder()
                    .region(Region.of(region))
                    .endpointOverride(URI.create(endpointOverride))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
        
        return SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

