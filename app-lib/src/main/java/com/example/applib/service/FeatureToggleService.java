package com.example.applib.service;

import com.example.applib.config.FeatureToggleConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for checking feature toggle status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureToggleService {

    private final FeatureToggleConfig.RedisFeatureProperties redisFeatureProperties;
    private final FeatureToggleConfig.KafkaFeatureProperties kafkaFeatureProperties;
    private final FeatureToggleConfig.S3FeatureProperties s3FeatureProperties;
    private final FeatureToggleConfig.SqsFeatureProperties sqsFeatureProperties;
    private final FeatureToggleConfig.MinioFeatureProperties minioFeatureProperties;

    @Value("${aws.secretsmanager.enabled:true}")
    private boolean secretsManagerEnabled;

    /**
     * Checks if Kafka is enabled.
     */
    public boolean isKafkaEnabled() {
        boolean enabled = kafkaFeatureProperties.isEnabled();
        log.debug("Kafka feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Checks if Redis is enabled.
     */
    public boolean isRedisEnabled() {
        boolean enabled = redisFeatureProperties.isEnabled();
        log.debug("Redis feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Checks if S3 is enabled.
     */
    public boolean isS3Enabled() {
        boolean enabled = s3FeatureProperties.isEnabled();
        log.debug("S3 feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Checks if SQS is enabled.
     */
    public boolean isSqsEnabled() {
        boolean enabled = sqsFeatureProperties.isEnabled();
        log.debug("SQS feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Checks if Minio is enabled.
     */
    public boolean isMinioEnabled() {
        boolean enabled = minioFeatureProperties.isEnabled();
        log.debug("Minio feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Checks if Secrets Manager is enabled.
     */
    public boolean isSecretsManagerEnabled() {
        log.debug("Secrets Manager feature is {}", secretsManagerEnabled ? "enabled" : "disabled");
        return secretsManagerEnabled;
    }
}
