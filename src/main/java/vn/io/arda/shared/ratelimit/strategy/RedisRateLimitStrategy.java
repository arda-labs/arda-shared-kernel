package vn.io.arda.shared.ratelimit.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based rate limiting implementation using token bucket algorithm.
 * <p>
 * This strategy stores rate limit counters in Redis, enabling distributed rate limiting
 * across multiple application instances. It uses Redis atomic operations (INCR) and
 * key expiration (TTL) to implement a sliding window counter.
 * </p>
 *
 * <p>Configuration example:</p>
 * <pre>
 * spring:
 *   redis:
 *     host: localhost
 *     port: 6379
 * </pre>
 *
 * <p>This implementation is only active when Redis is available on the classpath.</p>
 *
 * @author Arda Development Team
 * @see RateLimitStrategy
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(RedisTemplate.class)
public class RedisRateLimitStrategy implements RateLimitStrategy {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Override
    public boolean isAllowed(String key, int limit, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            // Increment counter atomically
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);

            if (currentCount == null) {
                log.error("Redis increment returned null for key: {}", redisKey);
                return true; // Fail open - allow request if Redis fails
            }

            // Set expiration on first request
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }

            boolean allowed = currentCount <= limit;

            if (!allowed) {
                log.debug("Rate limit exceeded for key: {} (count: {}, limit: {})",
                        key, currentCount, limit);
            }

            return allowed;

        } catch (Exception e) {
            log.error("Redis error during rate limit check for key: {}", key, e);
            return true; // Fail open - allow request if Redis fails
        }
    }

    @Override
    public void reset(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            redisTemplate.delete(redisKey);
            log.debug("Reset rate limit for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to reset rate limit for key: {}", key, e);
        }
    }

    @Override
    public long getCurrentCount(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            String value = redisTemplate.opsForValue().get(redisKey);
            return value != null ? Long.parseLong(value) : 0;
        } catch (Exception e) {
            log.error("Failed to get current count for key: {}", key, e);
            return 0;
        }
    }

    @Override
    public long getTimeUntilReset(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            log.error("Failed to get TTL for key: {}", key, e);
            return 0;
        }
    }
}
