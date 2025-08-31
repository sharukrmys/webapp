package com.example.applib.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * Configuration class for feature toggles.
 * This allows services to enable/disable features based on environment.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties
@PropertySources({
    @PropertySource("classpath:application.yml"),
    @PropertySource(value = "classpath:application-${spring.profiles.active}.yml", ignoreResourceNotFound = true)
})
public class FeatureToggleConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.redis")
    public RedisFeatureProperties redisFeatureProperties() {
        return new RedisFeatureProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.kafka")
    public KafkaFeatureProperties kafkaFeatureProperties() {
        return new KafkaFeatureProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "aws.s3")
    public S3FeatureProperties s3FeatureProperties() {
        return new S3FeatureProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "aws.sqs")
    public SqsFeatureProperties sqsFeatureProperties() {
        return new SqsFeatureProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "minio")
    public MinioFeatureProperties minioFeatureProperties() {
        return new MinioFeatureProperties();
    }

    @Data
    public static class RedisFeatureProperties {
        private boolean enabled = true;
        private String host;
        private int port;
        private String password;
    }

    @Data
    public static class KafkaFeatureProperties {
        private boolean enabled = true;
        private String bootstrapServers;
    }

    @Data
    public static class S3FeatureProperties {
        private boolean enabled = true;
        private String endpoint;
        private String bucketName;
    }

    @Data
    public static class SqsFeatureProperties {
        private boolean enabled = true;
        private String endpoint;
        private String queueName;
    }

    @Data
    public static class MinioFeatureProperties {
        private boolean enabled = true;
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
    }

    // Redis configuration that can be conditionally enabled/disabled
    @Configuration
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
    public static class RedisConfiguration {
        public RedisConfiguration() {
            log.info("Redis feature is enabled");
        }
    }

    // Kafka configuration that can be conditionally enabled/disabled
    @Configuration
    @ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
    public static class KafkaConfiguration {
        public KafkaConfiguration() {
            log.info("Kafka feature is enabled");
        }
    }

    // AWS S3 configuration that can be conditionally enabled/disabled
    @Configuration
    @ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
    public static class S3Configuration {
        public S3Configuration() {
            log.info("AWS S3 feature is enabled");
        }
    }

    // AWS SQS configuration that can be conditionally enabled/disabled
    @Configuration
    @ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true")
    public static class SqsConfiguration {
        public SqsConfiguration() {
            log.info("AWS SQS feature is enabled");
        }
    }

    // Minio configuration that can be conditionally enabled/disabled
    @Configuration
    @ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
    public static class MinioConfiguration {
        public MinioConfiguration() {
            log.info("Minio feature is enabled");
        }
    }
}
