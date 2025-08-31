package com.example.applib.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "Health check endpoints for testing application status")
public class HealthCheckController {

    @Value("${spring.application.name:app}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping
    @Operation(summary = "Get application health status", description = "Returns basic health information about the application")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", applicationName);
        response.put("profile", activeProfile);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/features")
    @Operation(summary = "Get feature flags", description = "Returns the status of all feature flags")
    public ResponseEntity<Map<String, Object>> getFeatureFlags(
            @Value("${features.redis.enabled:false}") boolean redisEnabled,
            @Value("${features.kafka.enabled:false}") boolean kafkaEnabled,
            @Value("${features.s3.enabled:false}") boolean s3Enabled,
            @Value("${features.sqs.enabled:false}") boolean sqsEnabled,
            @Value("${features.secretsmanager.enabled:false}") boolean secretsManagerEnabled) {
        
        Map<String, Object> features = new HashMap<>();
        features.put("redis", redisEnabled);
        features.put("kafka", kafkaEnabled);
        features.put("s3", s3Enabled);
        features.put("sqs", sqsEnabled);
        features.put("secretsManager", secretsManagerEnabled);
        
        return ResponseEntity.ok(features);
    }

    @GetMapping("/config")
    @Operation(summary = "Get configuration", description = "Returns the current configuration settings")
    public ResponseEntity<Map<String, Object>> getConfiguration(
            @Value("${spring.data.redis.host:localhost}") String redisHost,
            @Value("${spring.data.redis.port:6379}") int redisPort,
            @Value("${aws.region:us-east-1}") String awsRegion) {
        
        Map<String, Object> config = new HashMap<>();
        
        Map<String, Object> redis = new HashMap<>();
        redis.put("host", redisHost);
        redis.put("port", redisPort);
        
        Map<String, Object> aws = new HashMap<>();
        aws.put("region", awsRegion);
        
        config.put("redis", redis);
        config.put("aws", aws);
        
        return ResponseEntity.ok(config);
    }
}

