package com.example.applib.config;

import com.example.applib.service.FeatureToggleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "aws.secretsmanager.enabled", havingValue = "true")
public class SecretsManagerConfig {

    @Value("${aws.region}")
    private String region;

    @Autowired
    private AwsCredentialsProvider awsCredentialsProvider;

    @Autowired
    private FeatureToggleService featureToggleService;

    /**
     * Creates a Secrets Manager client using the AWS SDK v2.
     * Uses IAM instance profile credentials by default.
     *
     * @return SecretsManagerClient
     */
    @Bean
    @ConditionalOnProperty(name = "features.secretsmanager.enabled", havingValue = "true", matchIfMissing = true)
    public SecretsManagerClient secretsManagerClient() {
        log.info("Configuring Secrets Manager client with region: {}", region);

        if (!featureToggleService.isSecretsManagerEnabled()) {
            log.warn("Secrets Manager feature is disabled, but Secrets Manager client is being created. This may cause issues.");
        }

        return SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}
