package vn.io.arda.shared.cache.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for caching features.
 *
 * @since 0.0.1
 */
@Data
@ConfigurationProperties(prefix = "arda.shared.caching")
public class CachingProperties {

    /**
     * Enable or disable caching features.
     */
    private boolean enabled = true;

    /**
     * Redis configuration.
     */
    private RedisConfig redis = new RedisConfig();

    @Data
    public static class RedisConfig {
        /**
         * Redis host.
         */
        private String host = "localhost";

        /**
         * Redis port.
         */
        private int port = 6379;

        /**
         * Redis password (optional).
         */
        private String password;

        /**
         * Timeout in milliseconds.
         */
        private long timeout = 60000;
    }
}
