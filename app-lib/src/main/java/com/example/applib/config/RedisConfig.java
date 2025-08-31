package com.example.applib.config;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Value("${spring.redis.mode:standalone}")
    private String redisMode;

    // Standalone configuration
    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    // Sentinel configuration
    @Value("${spring.redis.sentinel.master:#{null}}")
    private String sentinelMaster;

    @Value("${spring.redis.sentinel.nodes:#{null}}")
    private List<String> sentinelNodes;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    // Pool configuration
    @Value("${spring.redis.lettuce.pool.max-active:8}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle:2}")
    private int minIdle;

    @Value("${spring.redis.lettuce.pool.max-wait:-1}")
    private long maxWait;

    @Value("${spring.redis.timeout:10000}")
    private long timeout;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettucePoolingClientConfiguration poolConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout))
                .poolConfig(new GenericObjectPoolConfig())
                .build();

        if ("sentinel".equalsIgnoreCase(redisMode) && sentinelMaster != null && sentinelNodes != null && !sentinelNodes.isEmpty()) {
            return createSentinelConnectionFactory(poolConfig);
        } else {
            return createStandaloneConnectionFactory(poolConfig);
        }
    }

    private LettuceConnectionFactory createStandaloneConnectionFactory(LettucePoolingClientConfiguration poolConfig) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            configuration.setPassword(RedisPassword.of(redisPassword));
        }
        return new LettuceConnectionFactory(configuration, poolConfig);
    }

    private LettuceConnectionFactory createSentinelConnectionFactory(LettucePoolingClientConfiguration poolConfig) {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.master(sentinelMaster);

        Set<String> sentinels = new HashSet<>(sentinelNodes);
        for (String node : sentinels) {
            String[] parts = node.split(":");
            sentinelConfig.sentinel(parts[0], Integer.parseInt(parts[1]));
        }

        if (redisPassword != null && !redisPassword.isEmpty()) {
            sentinelConfig.setPassword(RedisPassword.of(redisPassword));
        }

        return new LettuceConnectionFactory(sentinelConfig, poolConfig);
    }

    private GenericObjectPoolConfig<?> getPoolConfig() {
        GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(maxActive);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setMaxWait(Duration.ofMillis(maxWait));
        return config;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .transactionAware()
                .build();
    }
}
