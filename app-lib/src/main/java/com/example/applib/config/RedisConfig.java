package com.example.applib.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Value("${redis.host}")
    private String host;
    
    @Value("${redis.port}")
    private int port;
    
    @Value("${redis.password:#{null}}")
    private String password;
    
    @Value("${redis.database:0}")
    private int database;
    
    @Value("${redis.timeout:10000}")
    private int timeout;
    
    @Value("${redis.cache.ttl:3600}")
    private int cacheTtl;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        
        if (StringUtils.hasText(password)) {
            config.useSingleServer()
                    .setAddress(address)
                    .setPassword(password)
                    .setDatabase(database)
                    .setConnectTimeout(timeout);
        } else {
            config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(database)
                    .setConnectTimeout(timeout);
        }
        
        return Redisson.create(config);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "redis.cache.enabled", havingValue = "true", matchIfMissing = true)
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, org.redisson.spring.cache.CacheConfig> config = new HashMap<>();
        
        // Configure default cache TTL
        config.put("default", new org.redisson.spring.cache.CacheConfig(cacheTtl * 1000, cacheTtl * 500));
        
        // Add more cache configurations as needed
        config.put("users", new org.redisson.spring.cache.CacheConfig(3600 * 1000, 1800 * 1000));
        config.put("tenants", new org.redisson.spring.cache.CacheConfig(3600 * 1000, 1800 * 1000));
        
        return new RedissonSpringCacheManager(redissonClient, config);
    }
}

