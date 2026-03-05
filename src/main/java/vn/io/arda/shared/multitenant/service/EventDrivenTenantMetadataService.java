package vn.io.arda.shared.multitenant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import vn.io.arda.shared.exception.TenantNotFoundException;
import vn.io.arda.shared.multitenant.cache.TenantConfigLocalCache;
import vn.io.arda.shared.multitenant.model.TenantDataSourceConfig;

/**
 * Event-driven implementation of TenantMetadataService.
 * <p>
 * Reads tenant config from local in-memory cache (populated via Kafka events).
 * Zero network latency. Falls back to TenantNotFoundException if tenant is not
 * in cache (which means Central Platform hasn't published its config yet).
 * </p>
 *
 * @since 0.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "arda.shared.tenant-config.mode", havingValue = "event-driven")
public class EventDrivenTenantMetadataService implements TenantMetadataService {

  private final TenantConfigLocalCache configLocalCache;

  @Override
  public TenantDataSourceConfig getTenantDataSourceConfig(String tenantId) {
    return configLocalCache.get(tenantId)
        .orElseThrow(() -> {
          log.error("Tenant config not found in local cache: {}. " +
              "Tenant may not exist or Kafka event was missed. " +
              "Try restarting the service to trigger snapshot reload.", tenantId);
          return new TenantNotFoundException(tenantId);
        });
  }

  @Override
  public boolean tenantExists(String tenantId) {
    return configLocalCache.get(tenantId).isPresent();
  }
}
