package com.example.applib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.enabled:true}")
    private boolean awsEnabled;

    @Value("${aws.use-instance-profile:false}")
    private boolean useInstanceProfile;

    @Value("${aws.profile:default}")
    private String awsProfile;

    @Value("${aws.access-key:#{null}}")
    private String accessKey;

    @Value("${aws.secret-key:#{null}}")
    private String secretKey;

    @Bean
    public Region awsRegion() {
        return Region.of(awsRegion);
    }

    @Bean
    @Profile("!local")
    public AwsCredentialsProvider awsCredentialsProviderProd() {
        if (!awsEnabled) {
            return null;
        }

        if (useInstanceProfile) {
            // Use instance profile credentials (EC2/EKS)
            return DefaultCredentialsProvider.create();
        } else if (accessKey != null && secretKey != null) {
            // Use static credentials
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        } else {
            // Use profile credentials
            return ProfileCredentialsProvider.create(awsProfile);
        }
    }

    @Bean
    @Profile("local")
    public AwsCredentialsProvider awsCredentialsProviderLocal() {
        if (!awsEnabled) {
            return null;
        }

        if (accessKey != null && secretKey != null) {
            // Use static credentials
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        } else {
            // Use profile credentials
            return ProfileCredentialsProvider.create(awsProfile);
        }
    }
}
