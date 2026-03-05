package vn.io.arda.shared.event.tenant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.io.arda.shared.event.DomainEvent;

import java.time.Instant;

/**
 * Domain event published when a tenant's UI configuration is updated.
 * Triggers a notification to PLATFORM_ADMIN users.
 *
 * @since 0.0.1
 */
@Getter
@NoArgsConstructor
public class TenantUpdatedEvent implements DomainEvent {

  private final String eventType = "TENANT_UPDATED";
  private String tenantKey;
  private String displayName;
  private Instant occurredAt;

  @JsonCreator
  public TenantUpdatedEvent(
      @JsonProperty("tenantKey") String tenantKey,
      @JsonProperty("displayName") String displayName) {
    this.tenantKey = tenantKey;
    this.displayName = displayName;
    this.occurredAt = Instant.now();
  }
}
