package com.example.applib.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * Configuration class for AWS services.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AwsConfig {

    private final FeatureToggleConfig.S3FeatureProperties s3Properties;
    private final FeatureToggleConfig.SqsFeatureProperties sqsProperties;

    /**
     * Creates an S3 client for S3 operations.
     * This is conditionally enabled based on the aws.s3.enabled property.
     */
    @Bean
    @ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
    public S3Client s3Client() {
        log.info("Configuring AWS S3 client with endpoint: {}", s3Properties.getEndpoint());
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(s3Properties.getEndpoint()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Creates an SQS client for SQS operations.
     * This is conditionally enabled based on the aws.sqs.enabled property.
     */
    @Bean
    @ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true")
    public SqsClient sqsClient() {
        log.info("Configuring AWS SQS client with endpoint: {}", sqsProperties.getEndpoint());
        return SqsClient.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(sqsProperties.getEndpoint()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Creates a Secrets Manager client for Secrets Manager operations.
     * This is conditionally enabled based on the aws.secretsmanager.enabled property.
     */
    @Bean
    @ConditionalOnProperty(name = "aws.secretsmanager.enabled", havingValue = "true")
    public SecretsManagerClient secretsManagerClient() {
        log.info("Configuring AWS Secrets Manager client");
        return SecretsManagerClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

