package vn.io.arda.shared.multitenant.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.io.arda.shared.event.tenant.TenantConfigEvent;
import vn.io.arda.shared.multitenant.cache.TenantConfigLocalCache;
import vn.io.arda.shared.multitenant.datasource.TenantDataSourceCache;

/**
 * Kafka consumer that listens to tenant configuration events
 * and updates the local cache in real-time.
 * <p>
 * Topic: {@code arda.tenant-config-events}
 * </p>
 *
 * @since 0.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "arda.shared.tenant-config.mode", havingValue = "event-driven")
public class TenantConfigKafkaListener {

  private final TenantConfigLocalCache configLocalCache;
  private final TenantDataSourceCache dataSourceCache;
  private final ObjectMapper objectMapper;

  public static final String TOPIC = "arda.tenant-config-events";

  @KafkaListener(topics = TOPIC, groupId = "${spring.application.name:arda}-tenant-config")
  public void handleTenantConfigEvent(String message) {
    try {
      TenantConfigEvent event = objectMapper.readValue(message, TenantConfigEvent.class);
      log.info("Received tenant config event: type={}, tenant={}",
          event.getEventType(), event.getTenantKey());

      processEvent(event);

    } catch (Exception e) {
      log.error("Failed to process tenant config event: {}", message, e);
      // Don't rethrow — we don't want to block the consumer
      // Events are idempotent, so missing one is recoverable via snapshot
    }
  }

  private void processEvent(TenantConfigEvent event) {
    String tenantKey = event.getTenantKey();

    if (event.isCreated() || event.isUpdated()) {
      if (event.getConfig() != null) {
        configLocalCache.put(tenantKey, event.getConfig());
        // Evict old DataSource so it gets recreated with potentially new config
        dataSourceCache.evict(tenantKey);
        log.info("Tenant config {} for: {}", event.getEventType(), tenantKey);
      } else {
        log.warn("Received {} event without config for tenant: {}",
            event.getEventType(), tenantKey);
      }

    } else if (event.isDeleted()) {
      configLocalCache.evict(tenantKey);
      dataSourceCache.evict(tenantKey);
      log.info("Tenant config + datasource evicted for deleted tenant: {}", tenantKey);

    } else if (event.isLocked()) {
      configLocalCache.lockTenant(tenantKey);
      log.info("Tenant locked for maintenance: {}", tenantKey);

    } else if (event.isUnlocked()) {
      configLocalCache.unlockTenant(tenantKey);
      log.info("Tenant unlocked from maintenance: {}", tenantKey);

    } else {
      log.warn("Unknown tenant config event type: {}", event.getEventType());
    }
  }
}
