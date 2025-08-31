package com.example.applib.util;

import com.google.gson.Gson;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecretsManagerUtil {

    private final SecretsManagerClient secretsManagerClient;
    private final Gson gson;

    @Value("${aws.enabled:true}")
    private boolean awsEnabled;

    @Value("${aws.secretsmanager.enabled:true}")
    private boolean secretsManagerEnabled;

    /**
     * Get a secret value from AWS Secrets Manager
     *
     * @param secretName The name of the secret
     * @return The secret value as a string
     */
    @Cacheable(value = "secretsCache", key = "#secretName", unless = "#result == null")
    public String getSecretValue(String secretName) {
        if (!awsEnabled || !secretsManagerEnabled) {
            log.warn("AWS or SecretsManager is disabled. Secret retrieval skipped for: {}", secretName);
            return null;
        }

        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            return response.secretString();
        } catch (SecretsManagerException e) {
            log.error("Error retrieving secret from AWS Secrets Manager: {}", secretName, e);
            throw new RuntimeException("Failed to retrieve secret from AWS Secrets Manager", e);
        }
    }

    /**
     * Get a secret as a JSON object from AWS Secrets Manager
     *
     * @param secretName The name of the secret
     * @param clazz The class to deserialize the JSON to
     * @return The deserialized object
     */
    @Cacheable(value = "secretsCache", key = "#secretName + '-' + #clazz.simpleName", unless = "#result == null")
    public <T> T getSecretAsJson(String secretName, Class<T> clazz) {
        String secretValue = getSecretValue(secretName);
        if (secretValue == null) {
            return null;
        }

        try {
            return gson.fromJson(secretValue, clazz);
        } catch (Exception e) {
            log.error("Error deserializing secret JSON: {}", secretName, e);
            throw new RuntimeException("Failed to deserialize secret JSON", e);
        }
    }

    /**
     * Get a secret as a Map from AWS Secrets Manager
     *
     * @param secretName The name of the secret
     * @return The secret as a Map
     */
    @SuppressWarnings("unchecked")
    @Cacheable(value = "secretsCache", key = "#secretName + '-map'", unless = "#result == null")
    public Map<String, Object> getSecretAsMap(String secretName) {
        String secretValue = getSecretValue(secretName);
        if (secretValue == null) {
            return null;
        }

        try {
            return gson.fromJson(secretValue, Map.class);
        } catch (Exception e) {
            log.error("Error deserializing secret to Map: {}", secretName, e);
            throw new RuntimeException("Failed to deserialize secret to Map", e);
        }
    }
}
