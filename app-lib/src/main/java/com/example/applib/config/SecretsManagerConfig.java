package com.example.applib.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;

@Configuration
public class SecretsManagerConfig {

    @Value("${aws.enabled:true}")
    private boolean awsEnabled;

    @Value("${aws.secretsmanager.enabled:true}")
    private boolean secretsManagerEnabled;

    @Value("${aws.endpoint-override:#{null}}")
    private String endpointOverride;

    @Bean
    @Profile("!local")
    public SecretsManagerClient secretsManagerClientProd(Region awsRegion, AwsCredentialsProvider awsCredentialsProvider) {
        if (!awsEnabled || !secretsManagerEnabled) {
            return null;
        }

        SecretsManagerClientBuilder builder = SecretsManagerClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    @Bean
    @Profile("local")
    public SecretsManagerClient secretsManagerClientLocal(Region awsRegion, AwsCredentialsProvider awsCredentialsProvider) {
        if (!awsEnabled || !secretsManagerEnabled) {
            return null;
        }

        SecretsManagerClientBuilder builder = SecretsManagerClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }
}
