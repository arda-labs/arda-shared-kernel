package vn.io.arda.shared.event.tenant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.io.arda.shared.event.DomainEvent;
import vn.io.arda.shared.multitenant.model.TenantDataSourceConfig;

import java.time.Instant;

/**
 * Kafka event for tenant configuration changes.
 * Published by Central Platform, consumed by all services.
 *
 * @since 0.1.0
 */
@Getter
@NoArgsConstructor
public class TenantConfigEvent implements DomainEvent {

  private String eventType;
  private String tenantKey;
  private TenantDataSourceConfig config;
  private Instant occurredAt;

  @JsonCreator
  public TenantConfigEvent(
      @JsonProperty("eventType") String eventType,
      @JsonProperty("tenantKey") String tenantKey,
      @JsonProperty("config") TenantDataSourceConfig config) {
    this.eventType = eventType;
    this.tenantKey = tenantKey;
    this.config = config;
    this.occurredAt = Instant.now();
  }

  // Convenience factory methods
  public static TenantConfigEvent created(String tenantKey, TenantDataSourceConfig config) {
    return new TenantConfigEvent("CREATED", tenantKey, config);
  }

  public static TenantConfigEvent updated(String tenantKey, TenantDataSourceConfig config) {
    return new TenantConfigEvent("UPDATED", tenantKey, config);
  }

  public static TenantConfigEvent deleted(String tenantKey) {
    return new TenantConfigEvent("DELETED", tenantKey, null);
  }

  public static TenantConfigEvent locked(String tenantKey) {
    return new TenantConfigEvent("LOCKED", tenantKey, null);
  }

  public static TenantConfigEvent unlocked(String tenantKey) {
    return new TenantConfigEvent("UNLOCKED", tenantKey, null);
  }

  public boolean isCreated() {
    return "CREATED".equals(eventType);
  }

  public boolean isUpdated() {
    return "UPDATED".equals(eventType);
  }

  public boolean isDeleted() {
    return "DELETED".equals(eventType);
  }

  public boolean isLocked() {
    return "LOCKED".equals(eventType);
  }

  public boolean isUnlocked() {
    return "UNLOCKED".equals(eventType);
  }
}
