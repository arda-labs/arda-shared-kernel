package vn.io.arda.shared.ratelimit.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for rate limiting features.
 *
 * @since 0.0.1
 */
@Data
@ConfigurationProperties(prefix = "arda.shared.rate-limit")
public class RateLimitProperties {

    /**
     * Enable or disable rate limiting features.
     */
    private boolean enabled = true;

    /**
     * Default requests per minute.
     */
    private int defaultRequestsPerMinute = 60;

    /**
     * Storage backend: redis or memory.
     */
    private StorageType storage = StorageType.REDIS;

    public enum StorageType {
        REDIS,
        MEMORY
    }
}
