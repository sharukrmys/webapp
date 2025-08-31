package com.example.applib.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for feature toggles.
 * This allows enabling/disabling various features, especially useful for local development.
 */
@Configuration
@ConfigurationProperties(prefix = "features")
@Data
public class FeatureToggleConfig {

    private FeatureToggle kafka = new FeatureToggle();
    private FeatureToggle redis = new FeatureToggle();
    private FeatureToggle s3 = new FeatureToggle();
    private FeatureToggle sqs = new FeatureToggle();
    private FeatureToggle secretsmanager = new FeatureToggle();

    @Data
    public static class FeatureToggle {
        private boolean enabled = true;
    }
}
