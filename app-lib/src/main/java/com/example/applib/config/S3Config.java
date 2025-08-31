package com.example.applib.config;

import com.example.applib.service.FeatureToggleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3Config {

    @Value("${aws.region}")
    private String region;

    @Autowired
    private AwsCredentialsProvider awsCredentialsProvider;

    @Autowired
    private FeatureToggleService featureToggleService;

    /**
     * Creates an S3 client using the AWS SDK v2.
     * Uses IAM instance profile credentials by default.
     *
     * @return S3Client
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "features.s3.enabled", havingValue = "true", matchIfMissing = true)
    public S3Client s3Client() {
        log.info("Configuring S3 client with region: {}", region);

        if (!featureToggleService.isS3Enabled()) {
            log.warn("S3 feature is disabled, but S3 client is being created. This may cause issues.");
        }

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    /**
     * Creates an S3 presigner for generating pre-signed URLs.
     *
     * @return S3Presigner
     */
    @Bean
    @ConditionalOnProperty(name = "features.s3.enabled", havingValue = "true", matchIfMissing = true)
    public S3Presigner s3Presigner() {
        log.info("Configuring S3 presigner with region: {}", region);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}
