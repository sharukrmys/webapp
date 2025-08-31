package com.example.applib.config;

import com.example.applib.entity.TurboS3Config;
import com.example.applib.tenant.TenantContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class S3ConfigFromDb {

    private final JdbcTemplate tacJdbcTemplate;

    // Cache S3 configurations by tenant
    private final Map<String, TurboS3Config> s3ConfigCache = new ConcurrentHashMap<>();

    // Cache S3 clients by tenant
    private final Map<String, S3Client> s3ClientCache = new ConcurrentHashMap<>();

    // Cache S3 presigners by tenant
    private final Map<String, S3Presigner> s3PresignerCache = new ConcurrentHashMap<>();

    private static final String S3_CONFIG_QUERY =
            "SELECT awsaccesskeyid, awssecretaccesskey, region, bucketname, " +
            "schemaBucketName, imageBucketName, flexbucketname, " +
            "companyprofilebucketname, datamanagementbucketname " +
            "FROM turbos3config";

    /**
     * Get S3 configuration for the current tenant
     *
     * @return S3 configuration
     */
    public TurboS3Config getS3Config() {
        String tenantId = TenantContext.getTenantId();

        // Check cache first
        if (s3ConfigCache.containsKey(tenantId)) {
            return s3ConfigCache.get(tenantId);
        }

        try {
            List<TurboS3Config> s3ConfigList = tacJdbcTemplate.query(
                    S3_CONFIG_QUERY,
                    new BeanPropertyRowMapper<>(TurboS3Config.class)
            );

            if (s3ConfigList != null && !s3ConfigList.isEmpty()) {
                TurboS3Config s3Config = s3ConfigList.get(0);
                // Cache the config
                s3ConfigCache.put(tenantId, s3Config);
                return s3Config;
            } else {
                log.warn("No S3 configuration found for tenant: {}", tenantId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error retrieving S3 configuration for tenant: {}", tenantId, e);
            return null;
        }
    }

    /**
     * Get S3 client for the current tenant
     *
     * @return S3 client
     */
    @Bean
    @Primary
    public S3Client s3ClientFromDb() {
        String tenantId = TenantContext.getTenantId();

        // If no tenant context, return null
        if (tenantId == null) {
            log.warn("No tenant context found, returning null S3 client");
            return null;
        }

        // Check cache first
        if (s3ClientCache.containsKey(tenantId)) {
            return s3ClientCache.get(tenantId);
        }

        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            log.warn("No S3 configuration found for tenant: {}", tenantId);
            return null;
        }

        try {
            // Create AWS credentials
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    s3Config.getAwsAccessKeyId(),
                    s3Config.getAwsSecretAccessKey()
            );

            // Create S3 client
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(s3Config.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            // Cache the client
            s3ClientCache.put(tenantId, s3Client);

            return s3Client;
        } catch (Exception e) {
            log.error("Error creating S3 client for tenant: {}", tenantId, e);
            return null;
        }
    }

    /**
     * Get S3 presigner for the current tenant
     *
     * @return S3 presigner
     */
    @Bean
    @Primary
    public S3Presigner s3PresignerFromDb() {
        String tenantId = TenantContext.getTenantId();

        // If no tenant context, return null
        if (tenantId == null) {
            log.warn("No tenant context found, returning null S3 presigner");
            return null;
        }

        // Check cache first
        if (s3PresignerCache.containsKey(tenantId)) {
            return s3PresignerCache.get(tenantId);
        }

        TurboS3Config s3Config = getS3Config();
        if (s3Config == null) {
            log.warn("No S3 configuration found for tenant: {}", tenantId);
            return null;
        }

        try {
            // Create AWS credentials
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    s3Config.getAwsAccessKeyId(),
                    s3Config.getAwsSecretAccessKey()
            );

            // Create S3 presigner
            S3Presigner s3Presigner = S3Presigner.builder()
                    .region(Region.of(s3Config.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            // Cache the presigner
            s3PresignerCache.put(tenantId, s3Presigner);

            return s3Presigner;
        } catch (Exception e) {
            log.error("Error creating S3 presigner for tenant: {}", tenantId, e);
            return null;
        }
    }
}
