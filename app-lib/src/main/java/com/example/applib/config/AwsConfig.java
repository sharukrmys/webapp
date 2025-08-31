package com.example.applib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.enabled:true}")
    private boolean awsEnabled;

    @Value("${aws.use-instance-profile:true}")
    private boolean useInstanceProfile;

    @Value("${aws.profile:default}")
    private String awsProfile;

    @Value("${aws.access-key:#{null}}")
    private String accessKey;

    @Value("${aws.secret-key:#{null}}")
    private String secretKey;

    @Bean
    public Region awsRegion() {
        return Region.of(awsRegion);
    }

    /**
     * AWS Credentials Provider for non-local environments.
     * By default, uses EC2/EKS instance profile credentials.
     * Falls back to explicit credentials if configured.
     */
    @Bean
    @Profile("!local")
    public AwsCredentialsProvider awsCredentialsProviderProd() {
        if (!awsEnabled) {
            return null;
        }

        // Default to instance profile credentials (EC2/EKS/K8s)
        if (useInstanceProfile) {
            return DefaultCredentialsProvider.create();
        } 
        // Use explicit credentials if provided
        else if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        } 
        // Fall back to profile credentials
        else {
            return ProfileCredentialsProvider.create(awsProfile);
        }
    }

    /**
     * AWS Credentials Provider for local development.
     * Uses explicit credentials if provided, otherwise falls back to profile.
     */
    @Bean
    @Profile("local")
    public AwsCredentialsProvider awsCredentialsProviderLocal() {
        if (!awsEnabled) {
            return null;
        }

        // For local development, prefer explicit credentials
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        } 
        // Fall back to profile credentials
        else {
            return ProfileCredentialsProvider.create(awsProfile);
        }
    }
}

