package com.example.applib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true", matchIfMissing = true)
public class S3Config {

    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3Config(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String getBucket() {
        return bucket;
    }

    public S3Client getS3Client() {
        return s3Client;
    }
}

