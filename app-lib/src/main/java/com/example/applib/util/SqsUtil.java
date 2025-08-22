package com.example.applib.util;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true", matchIfMissing = true)
public class SqsUtil {

    private final SqsClient sqsClient;
    private final Gson gson;

    @Value("${aws.enabled:true}")
    private boolean awsEnabled;

    @Value("${aws.sqs.enabled:true}")
    private boolean sqsEnabled;

    /**
     * Send a message to an SQS queue
     * 
     * @param queueUrl The queue URL
     * @param message The message object (will be serialized to JSON)
     * @return The message ID
     */
    public String sendMessage(String queueUrl, Object message) {
        if (!awsEnabled || !sqsEnabled) {
            log.warn("AWS or SQS is disabled. Message not sent to queue: {}", queueUrl);
            return null;
        }

        try {
            String messageBody = gson.toJson(message);
            
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
            log.debug("Message sent to SQS queue: {}, message ID: {}", queueUrl, response.messageId());
            return response.messageId();
        } catch (Exception e) {
            log.error("Error sending message to SQS queue: {}", queueUrl, e);
            throw new RuntimeException("Failed to send message to SQS queue", e);
        }
    }

    /**
     * Send a message to an SQS queue with attributes
     * 
     * @param queueUrl The queue URL
     * @param message The message object (will be serialized to JSON)
     * @param attributes The message attributes
     * @return The message ID
     */
    public String sendMessage(String queueUrl, Object message, Map<String, MessageAttributeValue> attributes) {
        if (!awsEnabled || !sqsEnabled) {
            log.warn("AWS or SQS is disabled. Message not sent to queue: {}", queueUrl);
            return null;
        }

        try {
            String messageBody = gson.toJson(message);
            
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageAttributes(attributes)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
            log.debug("Message sent to SQS queue: {}, message ID: {}", queueUrl, response.messageId());
            return response.messageId();
        } catch (Exception e) {
            log.error("Error sending message to SQS queue: {}", queueUrl, e);
            throw new RuntimeException("Failed to send message to SQS queue", e);
        }
    }

    /**
     * Send a message to an SQS queue with delay
     * 
     * @param queueUrl The queue URL
     * @param message The message object (will be serialized to JSON)
     * @param delaySeconds The delay in seconds
     * @return The message ID
     */
    public String sendMessageWithDelay(String queueUrl, Object message, Integer delaySeconds) {
        if (!awsEnabled || !sqsEnabled) {
            log.warn("AWS or SQS is disabled. Message not sent to queue: {}", queueUrl);
            return null;
        }

        try {
            String messageBody = gson.toJson(message);
            
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .delaySeconds(delaySeconds)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
            log.debug("Message sent to SQS queue with delay: {}, message ID: {}", queueUrl, response.messageId());
            return response.messageId();
        } catch (Exception e) {
            log.error("Error sending message to SQS queue: {}", queueUrl, e);
            throw new RuntimeException("Failed to send message to SQS queue", e);
        }
    }

    /**
     * Receive messages from an SQS queue
     * 
     * @param queueUrl The queue URL
     * @param maxMessages The maximum number of messages to receive
     * @param waitTimeSeconds The wait time in seconds
     * @return List of messages
     */
    public List<Message> receiveMessages(String queueUrl, Integer maxMessages, Integer waitTimeSeconds) {
        if (!awsEnabled || !sqsEnabled) {
            log.warn("AWS or SQS is disabled. Messages not received from queue: {}", queueUrl);
            return List.of();
        }

        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(maxMessages)
                    .waitTimeSeconds(waitTimeSeconds)
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);
            log.debug("Received {} messages from SQS queue: {}", response.messages().size(), queueUrl);
            return response.messages();
        } catch (Exception e) {
            log.error("Error receiving messages from SQS queue: {}", queueUrl, e);
            throw new RuntimeException("Failed to receive messages from SQS queue", e);
        }
    }

    /**
     * Delete a message from an SQS queue
     * 
     * @param queueUrl The queue URL
     * @param receiptHandle The receipt handle
     */
    public void deleteMessage(String queueUrl, String receiptHandle) {
        if (!awsEnabled || !sqsEnabled) {
            log.warn("AWS or SQS is disabled. Message not deleted from queue: {}", queueUrl);
            return;
        }

        try {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();

            sqsClient.deleteMessage(deleteMessageRequest);
            log.debug("Message deleted from SQS queue: {}", queueUrl);
        } catch (Exception e) {
            log.error("Error deleting message from SQS queue: {}", queueUrl, e);
            throw new RuntimeException("Failed to delete message from SQS queue", e);
        }
    }

    /**
     * Delete multiple messages from an SQS queue
     * 
     * @param queueUrl The queue URL
     * @param messages The messages to delete
     */
    public void deleteMessages(String queueUrl, List<Message> messages) {
        if (!awsEnabled || !sqsEnabled) {
            log.warn("AWS or SQS is disabled. Messages not deleted from queue: {}", queueUrl);
            return;
        }

        try {
            List<DeleteMessageBatchRequestEntry> entries = messages.stream()
                    .map(message -> DeleteMessageBatchRequestEntry.builder()
                            .id(message.messageId())
                            .receiptHandle(message.receiptHandle())
                            .build())
                    .collect(Collectors.toList());

            DeleteMessageBatchRequest deleteMessageBatchRequest = DeleteMessageBatchRequest.builder()
                    .queueUrl(queueUrl)
                    .entries(entries)
                    .build();

            sqsClient.deleteMessageBatch(deleteMessageBatchRequest);
            log.debug("Deleted {} messages from SQS queue: {}", messages.size(), queueUrl);
        } catch (Exception e) {
            log.error("Error deleting messages from SQS queue: {}", queueUrl, e);
            throw new RuntimeException("Failed to delete messages from SQS queue", e);
        }
    }

    /**
     * Get the URL of an SQS queue by name
     * 
     * @param queueName The queue name
     * @return The queue URL
     */
    public String getQueueUrl(String queueName) {
        if (!awsEnabled || !sqsEnabled) {
            log.warn("AWS or SQS is disabled. Queue URL not retrieved for queue: {}", queueName);
            return null;
        }

        try {
            GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build();

            GetQueueUrlResponse response = sqsClient.getQueueUrl(getQueueUrlRequest);
            return response.queueUrl();
        } catch (Exception e) {
            log.error("Error getting URL for SQS queue: {}", queueName, e);
            throw new RuntimeException("Failed to get URL for SQS queue", e);
        }
    }

    /**
     * Create an SQS queue
     * 
     * @param queueName The queue name
     * @param attributes The queue attributes
     * @return The queue URL
     */
    public String createQueue(String queueName, Map<QueueAttributeName, String> attributes) {
        if (!awsEnabled || !sqsEnabled) {
            log.warn("AWS or SQS is disabled. Queue not created: {}", queueName);
            return null;
        }

        try {
            CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                    .queueName(queueName)
                    .attributes(attributes)
                    .build();

            CreateQueueResponse response = sqsClient.createQueue(createQueueRequest);
            log.info("Created SQS queue: {}, URL: {}", queueName, response.queueUrl());
            return response.queueUrl();
        } catch (Exception e) {
            log.error("Error creating SQS queue: {}", queueName, e);
            throw new RuntimeException("Failed to create SQS queue", e);
        }
    }
}
