package com.system.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CachingService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Cache a value with the specified key and TTL
     *
     * @param key        the cache key
     * @param value      the value to cache
     * @param ttlSeconds time to live in seconds
     * @param <T>        the type of the value
     */
    public <T> void cache(String key, T value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Cached value with key: {} (TTL: {}s)", key, ttlSeconds);
        } catch (Exception e) {
            log.error("Error caching value with key: {}", key, e);
        }
    }

    /**
     * Retrieve a cached value
     *
     * @param key   the cache key
     * @param clazz the class type to cast the value to
     * @param <T>   the type of the value
     * @return the cached value or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss for key: {}", key);
                return null;
            }
            log.debug("Cache hit for key: {}", key);
            return (T) value;
        } catch (Exception e) {
            log.error("Error retrieving cached value with key: {}", key, e);
            return null;
        }
    }

    /**
     * Retrieve a cached value without type checking
     *
     * @param key the cache key
     * @return the cached value or null if not found
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss for key: {}", key);
                return null;
            }
            log.debug("Cache hit for key: {}", key);
            return value;
        } catch (Exception e) {
            log.error("Error retrieving cached value with key: {}", key, e);
            return null;
        }
    }

    /**
     * Delete a cached value
     *
     * @param key the cache key
     * @return true if the key was deleted, false otherwise
     */
    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(result)) {
                log.debug("Deleted cache key: {}", key);
                return true;
            }
            log.debug("Cache key not found for deletion: {}", key);
            return false;
        } catch (Exception e) {
            log.error("Error deleting cache key: {}", key, e);
            return false;
        }
    }

    /**
     * Delete multiple cache keys matching a pattern
     *
     * @param pattern the pattern to match (e.g., "country:*")
     * @return the number of keys deleted
     */
    public long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                log.debug("No cache keys found matching pattern: {}", pattern);
                return 0;
            }
            Long deletedCount = redisTemplate.delete(keys);
            log.debug("Deleted {} cache keys matching pattern: {}", deletedCount, pattern);
            return deletedCount != null ? deletedCount : 0;
        } catch (Exception e) {
            log.error("Error deleting cache keys by pattern: {}", pattern, e);
            return 0;
        }
    }

    /**
     * Generate a cache key from components
     *
     * @param components the components to build the key
     * @return the cache key
     */
    public String buildKey(String... components) {
        return String.join(":", components);
    }
}
