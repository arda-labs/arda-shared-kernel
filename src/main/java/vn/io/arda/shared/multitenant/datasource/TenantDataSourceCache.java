package vn.io.arda.shared.multitenant.datasource;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import vn.io.arda.shared.multitenant.model.TenantDataSourceConfig;
import vn.io.arda.shared.multitenant.properties.MultiTenancyProperties;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * Cache for tenant DataSource instances with LRU eviction.
 * Uses Caffeine cache for efficient memory management.
 *
 * @since 0.0.1
 */
@Slf4j
public class TenantDataSourceCache {

    private final Cache<String, DataSource> cache;
    private final MultiTenancyProperties.ConnectionPoolConfig poolConfig;

    public TenantDataSourceCache(MultiTenancyProperties properties) {
        this.poolConfig = properties.getConnectionPool();

        MultiTenancyProperties.DataSourceCacheConfig cacheConfig = properties.getDatasourceCache();

        this.cache = Caffeine.newBuilder()
                .maximumSize(cacheConfig.getMaxSize())
                .expireAfterAccess(cacheConfig.getTtlMinutes(), TimeUnit.MINUTES)
                .removalListener(this::onDataSourceEvicted)
                .build();

        log.info("TenantDataSourceCache initialized with maxSize={}, ttl={}min",
                cacheConfig.getMaxSize(), cacheConfig.getTtlMinutes());
    }

    /**
     * Gets or creates a DataSource for the given tenant.
     *
     * @param tenantId the tenant ID
     * @param config the datasource configuration
     * @return the DataSource instance
     */
    public DataSource getOrCreate(String tenantId, TenantDataSourceConfig config) {
        return cache.get(tenantId, key -> createDataSource(tenantId, config));
    }

    /**
     * Removes a DataSource from the cache and closes it.
     *
     * @param tenantId the tenant ID
     */
    public void evict(String tenantId) {
        cache.invalidate(tenantId);
        log.info("DataSource evicted from cache: {}", tenantId);
    }

    /**
     * Clears all cached DataSources.
     */
    public void clear() {
        cache.invalidateAll();
        log.info("All DataSources cleared from cache");
    }

    /**
     * Gets cache statistics.
     */
    public long size() {
        return cache.estimatedSize();
    }

    private DataSource createDataSource(String tenantId, TenantDataSourceConfig config) {
        log.info("Creating new DataSource for tenant: {} ({})", tenantId, config.getDbType());

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClassName());

        // Connection pool settings
        hikariConfig.setMaximumPoolSize(poolConfig.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(poolConfig.getMinimumIdle());
        hikariConfig.setConnectionTimeout(poolConfig.getConnectionTimeout());

        // Pool name for monitoring
        hikariConfig.setPoolName("TenantPool-" + tenantId);

        // Additional settings
        hikariConfig.setAutoCommit(true);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setValidationTimeout(3000);

        return new HikariDataSource(hikariConfig);
    }

    private void onDataSourceEvicted(String tenantId, DataSource dataSource, RemovalCause cause) {
        log.info("DataSource evicted for tenant {}: {}", tenantId, cause);

        if (dataSource instanceof HikariDataSource hikariDataSource) {
            try {
                if (!hikariDataSource.isClosed()) {
                    // Log active connections before closing
                    int activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
                    int totalConnections = hikariDataSource.getHikariPoolMXBean().getTotalConnections();
                    int idleConnections = hikariDataSource.getHikariPoolMXBean().getIdleConnections();

                    if (activeConnections > 0) {
                        log.warn("Closing DataSource for tenant {} with {} active connections (total: {}, idle: {})",
                                tenantId, activeConnections, totalConnections, idleConnections);
                    }

                    hikariDataSource.close();
                    log.info("HikariDataSource successfully closed for tenant: {}", tenantId);
                }
            } catch (Exception e) {
                log.error("CRITICAL: Failed to close DataSource for tenant: {}. Potential connection leak! " +
                        "Manual intervention may be required.", tenantId, e);
                // Note: In production, you should integrate with monitoring/alerting system
                // Example: metricsService.incrementCounter("datasource.cleanup.failure", "tenant", tenantId);
            }
        }
    }
}
