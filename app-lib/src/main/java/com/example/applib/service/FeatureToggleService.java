package com.example.applib.service;

import com.example.applib.config.FeatureToggleConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service to check if features are enabled.
 * This is used to conditionally enable/disable features at runtime.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureToggleService {

    private final FeatureToggleConfig featureToggleConfig;

    /**
     * Check if Kafka is enabled.
     *
     * @return true if Kafka is enabled, false otherwise
     */
    public boolean isKafkaEnabled() {
        boolean enabled = featureToggleConfig.getKafka().isEnabled();
        log.debug("Kafka feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Check if Redis is enabled.
     *
     * @return true if Redis is enabled, false otherwise
     */
    public boolean isRedisEnabled() {
        boolean enabled = featureToggleConfig.getRedis().isEnabled();
        log.debug("Redis feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Check if S3 is enabled.
     *
     * @return true if S3 is enabled, false otherwise
     */
    public boolean isS3Enabled() {
        boolean enabled = featureToggleConfig.getS3().isEnabled();
        log.debug("S3 feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Check if SQS is enabled.
     *
     * @return true if SQS is enabled, false otherwise
     */
    public boolean isSqsEnabled() {
        boolean enabled = featureToggleConfig.getSqs().isEnabled();
        log.debug("SQS feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Check if Secrets Manager is enabled.
     *
     * @return true if Secrets Manager is enabled, false otherwise
     */
    public boolean isSecretsManagerEnabled() {
        boolean enabled = featureToggleConfig.getSecretsmanager().isEnabled();
        log.debug("Secrets Manager feature is {}", enabled ? "enabled" : "disabled");
        return enabled;
    }
}
