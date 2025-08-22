package com.example.applib.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.AthenaClientBuilder;

@Configuration
public class AthenaConfig {

    @Value("${aws.enabled:true}")
    private boolean awsEnabled;

    @Value("${aws.athena.enabled:true}")
    private boolean athenaEnabled;

    @Value("${aws.athena.output-location:s3://aws-athena-query-results/}")
    private String outputLocation;

    @Value("${aws.endpoint-override:#{null}}")
    private String endpointOverride;

    @Bean
    @Profile("!local")
    public AthenaClient athenaClientProd(Region awsRegion, AwsCredentialsProvider awsCredentialsProvider) {
        if (!awsEnabled || !athenaEnabled) {
            return null;
        }

        AthenaClientBuilder builder = AthenaClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    @Bean
    @Profile("local")
    public AthenaClient athenaClientLocal(Region awsRegion, AwsCredentialsProvider awsCredentialsProvider) {
        if (!awsEnabled || !athenaEnabled) {
            return null;
        }

        AthenaClientBuilder builder = AthenaClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    @Bean
    public String athenaOutputLocation() {
        return outputLocation;
    }
}
