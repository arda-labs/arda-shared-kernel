package vn.io.arda.shared.multitenant.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for multi-tenancy features.
 *
 * @since 0.0.1
 */
@Data
@ConfigurationProperties(prefix = "arda.shared.multi-tenancy")
public class MultiTenancyProperties {

    /**
     * Enable or disable multi-tenancy features.
     */
    private boolean enabled = true;

    /**
     * HTTP header name for tenant ID.
     */
    private String tenantHeader = "X-Tenant-ID";

    /**
     * URL of the central platform service for tenant metadata.
     */
    private String centralPlatformUrl = "http://localhost:8000";

    /**
     * DataSource cache configuration.
     */
    private DataSourceCacheConfig datasourceCache = new DataSourceCacheConfig();

    /**
     * Connection pool configuration for tenant databases.
     */
    private ConnectionPoolConfig connectionPool = new ConnectionPoolConfig();

    @Data
    public static class DataSourceCacheConfig {
        /**
         * Maximum number of datasources to cache.
         */
        private int maxSize = 100;

        /**
         * Time-to-live for cached datasources in minutes.
         */
        private long ttlMinutes = 60;
    }

    @Data
    public static class ConnectionPoolConfig {
        /**
         * Maximum pool size per tenant database.
         */
        private int maximumPoolSize = 5;

        /**
         * Minimum idle connections.
         */
        private int minimumIdle = 2;

        /**
         * Connection timeout in milliseconds.
         */
        private long connectionTimeout = 30000;
    }
}
