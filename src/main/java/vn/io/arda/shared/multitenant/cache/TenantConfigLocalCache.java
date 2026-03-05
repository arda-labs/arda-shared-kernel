package vn.io.arda.shared.multitenant.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import vn.io.arda.shared.multitenant.model.TenantDataSourceConfig;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory local cache for tenant configuration.
 * <p>
 * Populated at startup via snapshot REST call to Central Platform,
 * then kept up-to-date via Kafka events (TenantConfigEvent).
 * This eliminates per-request REST calls to Central Platform.
 * </p>
 *
 * @since 0.1.0
 */
@Slf4j
public class TenantConfigLocalCache {

  private final Cache<String, TenantDataSourceConfig> configCache;
  private final Set<String> maintenanceTenants = ConcurrentHashMap.newKeySet();

  public TenantConfigLocalCache(int maxSize) {
    this.configCache = Caffeine.newBuilder()
        .maximumSize(maxSize)
        .build();
    log.info("TenantConfigLocalCache initialized (maxSize={})", maxSize);
  }

  /**
   * Gets tenant config from local cache.
   *
   * @param tenantKey the tenant key
   * @return Optional containing the config, or empty if not cached
   */
  public Optional<TenantDataSourceConfig> get(String tenantKey) {
    return Optional.ofNullable(configCache.getIfPresent(tenantKey));
  }

  /**
   * Upserts tenant config into cache.
   */
  public void put(String tenantKey, TenantDataSourceConfig config) {
    configCache.put(tenantKey, config);
    log.debug("Tenant config cached: {}", tenantKey);
  }

  /**
   * Removes tenant config from cache.
   */
  public void evict(String tenantKey) {
    configCache.invalidate(tenantKey);
    maintenanceTenants.remove(tenantKey);
    log.info("Tenant config evicted: {}", tenantKey);
  }

  /**
   * Bulk-loads tenant configs (used for initial snapshot from Central Platform).
   */
  public void loadAll(Map<String, TenantDataSourceConfig> configs) {
    configCache.putAll(configs);
    log.info("Bulk-loaded {} tenant configs into cache", configs.size());
  }

  /**
   * Marks a tenant as "in maintenance" (will reject requests with 503).
   */
  public void lockTenant(String tenantKey) {
    maintenanceTenants.add(tenantKey);
    log.info("Tenant LOCKED for maintenance: {}", tenantKey);
  }

  /**
   * Removes maintenance flag for a tenant.
   */
  public void unlockTenant(String tenantKey) {
    maintenanceTenants.remove(tenantKey);
    log.info("Tenant UNLOCKED from maintenance: {}", tenantKey);
  }

  /**
   * Checks if a tenant is currently in maintenance mode.
   */
  public boolean isInMaintenance(String tenantKey) {
    return maintenanceTenants.contains(tenantKey);
  }

  /**
   * Gets total cached tenant count.
   */
  public long size() {
    return configCache.estimatedSize();
  }

  /**
   * Gets count of tenants in maintenance mode.
   */
  public int maintenanceCount() {
    return maintenanceTenants.size();
  }
}
