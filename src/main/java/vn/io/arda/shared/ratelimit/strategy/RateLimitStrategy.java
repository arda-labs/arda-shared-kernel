package vn.io.arda.shared.ratelimit.strategy;

/**
 * Strategy interface for implementing rate limiting algorithms.
 * <p>
 * Implementations of this interface provide different rate limiting strategies
 * such as token bucket, sliding window, or fixed window algorithms.
 * </p>
 *
 * <p>The strategy can be backed by different storage mechanisms:</p>
 * <ul>
 *   <li>Redis - for distributed rate limiting across multiple instances</li>
 *   <li>In-memory - for single-instance applications or testing</li>
 * </ul>
 *
 * @author Arda Development Team
 * @see RedisRateLimitStrategy
 * @see InMemoryRateLimitStrategy
 */
public interface RateLimitStrategy {

    /**
     * Checks if a request is allowed based on the rate limit configuration.
     *
     * @param key           the unique key identifying the rate limit bucket (e.g., "tenant:api:endpoint")
     * @param limit         the maximum number of requests allowed in the time window
     * @param windowSeconds the time window in seconds
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    boolean isAllowed(String key, int limit, int windowSeconds);

    /**
     * Resets the rate limit counter for a specific key.
     * Useful for manual intervention or testing purposes.
     *
     * @param key the unique key identifying the rate limit bucket
     */
    void reset(String key);

    /**
     * Gets the current count of requests for a specific key.
     *
     * @param key the unique key identifying the rate limit bucket
     * @return the current request count, or 0 if the key doesn't exist
     */
    long getCurrentCount(String key);

    /**
     * Gets the remaining time (in seconds) until the rate limit window resets.
     *
     * @param key the unique key identifying the rate limit bucket
     * @return the remaining seconds, or 0 if the key doesn't exist
     */
    long getTimeUntilReset(String key);
}
