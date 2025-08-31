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
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true")
public class SqsConfig {

    @Value("${aws.region}")
    private String region;

    @Autowired
    private AwsCredentialsProvider awsCredentialsProvider;

    @Autowired
    private FeatureToggleService featureToggleService;

    /**
     * Creates an SQS client using the AWS SDK v2.
     * Uses IAM instance profile credentials by default.
     *
     * @return SqsClient
     */
    @Bean
    @ConditionalOnProperty(name = "features.sqs.enabled", havingValue = "true", matchIfMissing = true)
    public SqsClient sqsClient() {
        log.info("Configuring SQS client with region: {}", region);

        if (!featureToggleService.isSqsEnabled()) {
            log.warn("SQS feature is disabled, but SQS client is being created. This may cause issues.");
        }

        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}
