package com.crms.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token blacklist for logout invalidation.
 * Uses Redis when available (via Spring autowiring), falls back to
 * an in-process ConcurrentHashMap so the app works without Redis.
 * 
 * Tokens are blacklisted by their JTI (JWT ID) claim.
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private static final String REDIS_KEY_PREFIX = "crms:jwt:blacklist:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(25);

    // In-memory fallback — ConcurrentHashMap is thread-safe
    private final ConcurrentHashMap<String, Long> inMemoryBlacklist = new ConcurrentHashMap<>();

    private final RedisTemplate<String, String> redisTemplate;
    private volatile boolean redisAvailable = false;

    public TokenBlacklistService(
            @Autowired(required = false) RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        if (redisTemplate != null) {
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                redisAvailable = true;
                log.info("TokenBlacklistService: Redis backend active");
            } catch (Exception e) {
                log.warn("TokenBlacklistService: Redis unavailable, using in-memory fallback");
                redisAvailable = false;
            }
        } else {
            log.info("TokenBlacklistService: Redis not configured, using in-memory store");
        }
    }

    /**
     * Blacklist a token by its unique identifier (JTI).
     * @param tokenId the JTI from the JWT claims
     * @param expiresAtEpochSec token expiry as epoch seconds (used to set TTL)
     */
    public void blacklist(String tokenId, long expiresAtEpochSec) {
        if (redisAvailable) {
            try {
                long ttlSeconds = Math.max(1, expiresAtEpochSec - (System.currentTimeMillis() / 1000) + 3600);
                redisTemplate.opsForValue().set(
                    REDIS_KEY_PREFIX + tokenId,
                    "1",
                    Duration.ofSeconds(ttlSeconds)
                );
                log.debug("Token blacklisted (Redis): {}", tokenId);
                return;
            } catch (Exception e) {
                log.warn("Redis blacklist write failed, falling back to in-memory: {}", e.getMessage());
            }
        }
        inMemoryBlacklist.put(tokenId, expiresAtEpochSec);
        log.debug("Token blacklisted (memory): {}", tokenId);
    }

    /**
     * Check if a token is blacklisted.
     */
    public boolean isBlacklisted(String tokenId) {
        if (redisAvailable) {
            try {
                Boolean exists = redisTemplate.hasKey(REDIS_KEY_PREFIX + tokenId);
                if (exists != null && exists) return true;
            } catch (Exception e) {
                log.warn("Redis blacklist check failed: {}", e.getMessage());
            }
        }
        Long expiry = inMemoryBlacklist.get(tokenId);
        if (expiry == null) return false;
        // Auto-evict expired entries
        if (expiry < System.currentTimeMillis() / 1000) {
            inMemoryBlacklist.remove(tokenId);
            return false;
        }
        return true;
    }

    /**
     * Get count of blacklisted tokens (for monitoring).
     */
    public long getBlacklistSize() {
        if (redisAvailable) {
            try {
                java.util.Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
                return keys != null ? keys.size() : 0L;
            } catch (Exception e) {
                return inMemoryBlacklist.size();
            }
        }
        long now = System.currentTimeMillis() / 1000;
        inMemoryBlacklist.entrySet().removeIf(e -> e.getValue() < now);
        return inMemoryBlacklist.size();
    }
}
