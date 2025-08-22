package com.example.applib.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

@Configuration
public class SqsConfig {

    @Value("${aws.enabled:true}")
    private boolean awsEnabled;

    @Value("${aws.sqs.enabled:true}")
    private boolean sqsEnabled;

    @Value("${aws.endpoint-override:#{null}}")
    private String endpointOverride;

    @Bean
    @Profile("!local")
    public SqsClient sqsClientProd(Region awsRegion, AwsCredentialsProvider awsCredentialsProvider) {
        if (!awsEnabled || !sqsEnabled) {
            return null;
        }

        SqsClientBuilder builder = SqsClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    @Bean
    @Profile("local")
    public SqsClient sqsClientLocal(Region awsRegion, AwsCredentialsProvider awsCredentialsProvider) {
        if (!awsEnabled || !sqsEnabled) {
            return null;
        }

        SqsClientBuilder builder = SqsClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }
}
