package com.example.applib.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaUtil {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.enabled:true}")
    private boolean kafkaEnabled;

    @Value("${kafka.send.timeout-seconds:30}")
    private long sendTimeoutSeconds;

    /**
     * Send a message to a Kafka topic asynchronously
     *
     * @param topic The topic name
     * @param key The message key
     * @param message The message payload
     * @return CompletableFuture of SendResult
     */
    public CompletableFuture<SendResult<String, Object>> sendAsync(String topic, String key, Object message) {
        if (!kafkaEnabled) {
            log.warn("Kafka is disabled. Message not sent to topic: {}", topic);
            return CompletableFuture.failedFuture(new IllegalStateException("Kafka is disabled"));
        }

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Message sent successfully to topic: {}, partition: {}, offset: {}",
                            topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send message to topic: {}", topic, ex);
                }
            });

            return future;
        } catch (Exception e) {
            log.error("Error sending message to Kafka topic: {}", topic, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send a message to a Kafka topic synchronously
     *
     * @param topic The topic name
     * @param key The message key
     * @param message The message payload
     * @return SendResult
     */
    public SendResult<String, Object> sendSync(String topic, String key, Object message) {
        if (!kafkaEnabled) {
            log.warn("Kafka is disabled. Message not sent to topic: {}", topic);
            throw new IllegalStateException("Kafka is disabled");
        }

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);
            return future.get(sendTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while sending message to Kafka topic: {}", topic, e);
            throw new RuntimeException("Thread interrupted while sending message to Kafka", e);
        } catch (ExecutionException | TimeoutException e) {
            log.error("Error sending message to Kafka topic: {}", topic, e);
            throw new RuntimeException("Failed to send message to Kafka", e);
        }
    }

    /**
     * Send a message to a Kafka topic with no key asynchronously
     *
     * @param topic The topic name
     * @param message The message payload
     * @return CompletableFuture of SendResult
     */
    public CompletableFuture<SendResult<String, Object>> sendAsync(String topic, Object message) {
        return sendAsync(topic, null, message);
    }

    /**
     * Send a message to a Kafka topic with no key synchronously
     *
     * @param topic The topic name
     * @param message The message payload
     * @return SendResult
     */
    public SendResult<String, Object> sendSync(String topic, Object message) {
        return sendSync(topic, null, message);
    }
}
