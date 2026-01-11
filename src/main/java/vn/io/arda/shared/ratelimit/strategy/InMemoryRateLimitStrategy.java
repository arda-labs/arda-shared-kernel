package vn.io.arda.shared.ratelimit.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiting implementation using sliding window algorithm.
 * <p>
 * This fallback strategy stores rate limit counters in local memory using ConcurrentHashMap.
 * It is suitable for single-instance applications or testing environments where Redis is not available.
 * </p>
 *
 * <p><strong>Warning:</strong> This implementation does NOT provide distributed rate limiting.
 * Each application instance maintains its own counters. For production multi-instance deployments,
 * use {@link RedisRateLimitStrategy} instead.</p>
 *
 * <p>This implementation is only active when RedisRateLimitStrategy is not available.</p>
 *
 * @author Arda Development Team
 * @see RateLimitStrategy
 * @see RedisRateLimitStrategy
 */
@Slf4j
@Component
@ConditionalOnMissingBean(RedisRateLimitStrategy.class)
public class InMemoryRateLimitStrategy implements RateLimitStrategy {

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean isAllowed(String key, int limit, int windowSeconds) {
        RateLimitBucket bucket = buckets.computeIfAbsent(key, k -> new RateLimitBucket());

        synchronized (bucket) {
            long now = Instant.now().getEpochSecond();
            long windowStart = now - windowSeconds;

            // Reset if window has expired
            if (bucket.windowStart < windowStart) {
                bucket.reset(now);
            }

            int currentCount = bucket.count.incrementAndGet();
            boolean allowed = currentCount <= limit;

            if (!allowed) {
                log.debug("Rate limit exceeded for key: {} (count: {}, limit: {})",
                        key, currentCount, limit);
            }

            return allowed;
        }
    }

    @Override
    public void reset(String key) {
        buckets.remove(key);
        log.debug("Reset rate limit for key: {}", key);
    }

    @Override
    public long getCurrentCount(String key) {
        RateLimitBucket bucket = buckets.get(key);
        return bucket != null ? bucket.count.get() : 0;
    }

    @Override
    public long getTimeUntilReset(String key) {
        RateLimitBucket bucket = buckets.get(key);
        if (bucket == null) {
            return 0;
        }

        long now = Instant.now().getEpochSecond();
        long elapsed = now - bucket.windowStart;
        return Math.max(0, bucket.windowSeconds - elapsed);
    }

    /**
     * Internal class representing a rate limit bucket.
     */
    private static class RateLimitBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private long windowStart;
        private int windowSeconds;

        void reset(long currentTime) {
            this.count.set(0);
            this.windowStart = currentTime;
        }
    }
}
