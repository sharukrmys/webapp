package com.example.applib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true", matchIfMissing = true)
public class SqsConfig {

    private final SqsClient sqsClient;
    
    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public SqsConfig(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public SqsClient getSqsClient() {
        return sqsClient;
    }
}

