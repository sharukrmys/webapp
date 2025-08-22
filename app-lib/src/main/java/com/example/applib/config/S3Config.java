package com.example.applib.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.enabled:true}")
    private boolean awsEnabled;

    @Value("${aws.s3.enabled:true}")
    private boolean s3Enabled;

    @Value("${aws.endpoint-override:#{null}}")
    private String endpointOverride;

    @Bean
    @Profile("!local")
    public S3Client s3ClientProd(Region awsRegion, AwsCredentialsProvider awsCredentialsProvider) {
        if (!awsEnabled || !s3Enabled) {
            return null;
        }

        S3ClientBuilder builder = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    @Bean
    @Profile("local")
    public S3Client s3ClientLocal(Region awsRegion, AwsCredentialsProvider awsCredentialsProvider) {
        if (!awsEnabled || !s3Enabled) {
            return null;
        }

        S3ClientBuilder builder = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(Region awsRegion, AwsCredentialsProvider awsCredentialsProvider) {
        if (!awsEnabled || !s3Enabled) {
            return null;
        }

        S3Presigner.Builder builder = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }
}
