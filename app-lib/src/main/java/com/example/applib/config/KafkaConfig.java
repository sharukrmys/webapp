package com.example.applib.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group-id:app-consumer-group}")
    private String consumerGroupId;

    @Value("${kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${kafka.consumer.max-poll-records:500}")
    private int maxPollRecords;

    @Value("${kafka.producer.acks:all}")
    private String acks;

    @Value("${kafka.producer.retries:3}")
    private int retries;

    @Value("${kafka.producer.batch-size:16384}")
    private int batchSize;

    @Value("${kafka.producer.buffer-memory:33554432}")
    private int bufferMemory;

    @Value("${kafka.security.protocol:#{null}}")
    private String securityProtocol;

    @Value("${kafka.sasl.mechanism:#{null}}")
    private String saslMechanism;

    @Value("${kafka.sasl.jaas.config:#{null}}")
    private String saslJaasConfig;

    @Value("${kafka.ssl.truststore.location:#{null}}")
    private String sslTruststoreLocation;

    @Value("${kafka.ssl.truststore.password:#{null}}")
    private String sslTruststorePassword;

    @Bean
    @Profile("!local")
    public Map<String, Object> producerConfigsProd() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);

        // Add security configurations if provided
        if (securityProtocol != null && !securityProtocol.isEmpty()) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        }

        if (saslMechanism != null && !saslMechanism.isEmpty()) {
            props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        }

        if (saslJaasConfig != null && !saslJaasConfig.isEmpty()) {
            props.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
        }

        if (sslTruststoreLocation != null && !sslTruststoreLocation.isEmpty()) {
            props.put("ssl.truststore.location", sslTruststoreLocation);
        }

        if (sslTruststorePassword != null && !sslTruststorePassword.isEmpty()) {
            props.put("ssl.truststore.password", sslTruststorePassword);
        }

        return props;
    }

    @Bean
    @Profile("local")
    public Map<String, Object> producerConfigsLocal() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(Map<String, Object> producerConfigs) {
        return new DefaultKafkaProducerFactory<>(producerConfigs);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Profile("!local")
    public Map<String, Object> consumerConfigsProd() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Add security configurations if provided
        if (securityProtocol != null && !securityProtocol.isEmpty()) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        }

        if (saslMechanism != null && !saslMechanism.isEmpty()) {
            props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        }

        if (saslJaasConfig != null && !saslJaasConfig.isEmpty()) {
            props.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
        }

        if (sslTruststoreLocation != null && !sslTruststoreLocation.isEmpty()) {
            props.put("ssl.truststore.location", sslTruststoreLocation);
        }

        if (sslTruststorePassword != null && !sslTruststorePassword.isEmpty()) {
            props.put("ssl.truststore.password", sslTruststorePassword);
        }

        return props;
    }

    @Bean
    @Profile("local")
    public Map<String, Object> consumerConfigsLocal() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(Map<String, Object> consumerConfigs) {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
