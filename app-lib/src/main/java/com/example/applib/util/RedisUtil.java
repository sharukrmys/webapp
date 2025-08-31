package com.example.applib.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisUtil {

    private final RedissonClient redissonClient;

    @Value("${spring.data.redis.lock.wait-time:10}")
    private int lockWaitTime;

    @Value("${spring.data.redis.lock.lease-time:30}")
    private int lockLeaseTime;

    /**
     * Publish a message to a Redis topic
     *
     * @param topic The topic name
     * @param message The message to publish
     * @return The number of subscribers that received the message
     */
    public long publish(String topic, Object message) {
        try {
            RTopic rTopic = redissonClient.getTopic(topic);
            return rTopic.publish(message);
        } catch (Exception e) {
            log.error("Error publishing message to Redis topic: {}", topic, e);
            throw new RuntimeException("Failed to publish message to Redis topic", e);
        }
    }

    /**
     * Subscribe to a Redis topic
     *
     * @param topic The topic name
     * @param messageClass The class of the message
     * @param messageHandler The message handler
     * @return The subscription ID
     */
    public <T> int subscribe(String topic, Class<T> messageClass, Consumer<T> messageHandler) {
        try {
            RTopic rTopic = redissonClient.getTopic(topic);
            return rTopic.addListener(messageClass, (channel, msg) -> messageHandler.accept(msg));
        } catch (Exception e) {
            log.error("Error subscribing to Redis topic: {}", topic, e);
            throw new RuntimeException("Failed to subscribe to Redis topic", e);
        }
    }

    /**
     * Unsubscribe from a Redis topic
     *
     * @param topic The topic name
     * @param listenerId The listener ID
     */
    public void unsubscribe(String topic, int listenerId) {
        try {
            RTopic rTopic = redissonClient.getTopic(topic);
            rTopic.removeListener(listenerId);
        } catch (Exception e) {
            log.error("Error unsubscribing from Redis topic: {}", topic, e);
            throw new RuntimeException("Failed to unsubscribe from Redis topic", e);
        }
    }

    /**
     * Add a message to a Redis queue
     *
     * @param queueName The queue name
     * @param message The message to add
     */
    public <T> void addToQueue(String queueName, T message) {
        try {
            RBlockingQueue<T> queue = redissonClient.getBlockingQueue(queueName);
            queue.add(message);
        } catch (Exception e) {
            log.error("Error adding message to Redis queue: {}", queueName, e);
            throw new RuntimeException("Failed to add message to Redis queue", e);
        }
    }

    /**
     * Take a message from a Redis queue (blocking operation)
     *
     * @param queueName The queue name
     * @param timeout The timeout in seconds
     * @return The message or null if timeout
     */
    public <T> T takeFromQueue(String queueName, Class<T> messageClass, long timeout) {
        try {
            RBlockingQueue<T> queue = redissonClient.getBlockingQueue(queueName);
            return queue.poll(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error taking message from Redis queue: {}", queueName, e);
            throw new RuntimeException("Failed to take message from Redis queue", e);
        }
    }

    /**
     * Add a delayed message to a Redis queue
     *
     * @param queueName The queue name
     * @param message The message to add
     * @param delay The delay in seconds
     */
    public <T> void addDelayedToQueue(String queueName, T message, long delay) {
        try {
            RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(redissonClient.getBlockingQueue(queueName));
            delayedQueue.offer(message, delay, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error adding delayed message to Redis queue: {}", queueName, e);
            throw new RuntimeException("Failed to add delayed message to Redis queue", e);
        }
    }

    /**
     * Acquire a distributed lock
     *
     * @param lockName The lock name
     * @return true if lock acquired, false otherwise
     */
    public boolean acquireLock(String lockName) {
        try {
            RLock lock = redissonClient.getLock(lockName);
            return lock.tryLock(lockWaitTime, lockLeaseTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error acquiring Redis lock: {}", lockName, e);
            throw new RuntimeException("Failed to acquire Redis lock", e);
        }
    }

    /**
     * Release a distributed lock
     *
     * @param lockName The lock name
     */
    public void releaseLock(String lockName) {
        try {
            RLock lock = redissonClient.getLock(lockName);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("Error releasing Redis lock: {}", lockName, e);
            throw new RuntimeException("Failed to release Redis lock", e);
        }
    }

    /**
     * Execute a function with a distributed lock
     *
     * @param lockName The lock name
     * @param function The function to execute
     * @return The function result
     */
    public <T> T executeWithLock(String lockName, java.util.function.Supplier<T> function) {
        boolean locked = false;
        try {
            locked = acquireLock(lockName);
            if (!locked) {
                throw new RuntimeException("Failed to acquire lock: " + lockName);
            }
            return function.get();
        } finally {
            if (locked) {
                releaseLock(lockName);
            }
        }
    }

    /**
     * Get a distributed rate limiter
     *
     * @param name The rate limiter name
     * @param rate The rate (permits per second)
     * @param rateInterval The rate interval in seconds
     * @return The rate limiter
     */
    public RRateLimiter getRateLimiter(String name, long rate, long rateInterval) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(name);
        rateLimiter.trySetRate(RateType.OVERALL, rate, rateInterval, RateIntervalUnit.SECONDS);
        return rateLimiter;
    }

    /**
     * Get a distributed semaphore
     *
     * @param name The semaphore name
     * @param permits The number of permits
     * @return The semaphore
     */
    public RSemaphore getSemaphore(String name, int permits) {
        RSemaphore semaphore = redissonClient.getSemaphore(name);
        semaphore.trySetPermits(permits);
        return semaphore;
    }

    /**
     * Get a distributed map
     *
     * @param name The map name
     * @return The map
     */
    public <K, V> RMap<K, V> getMap(String name) {
        return redissonClient.getMap(name);
    }

    /**
     * Get a distributed map with TTL
     *
     * @param name The map name
     * @param ttl The TTL in seconds
     * @return The map
     */
    public <K, V> RMapCache<K, V> getMapCache(String name, long ttl) {
        RMapCache<K, V> map = redissonClient.getMapCache(name);
        map.setMaxSize(1000); // Limit cache size
        return map;
    }
}
