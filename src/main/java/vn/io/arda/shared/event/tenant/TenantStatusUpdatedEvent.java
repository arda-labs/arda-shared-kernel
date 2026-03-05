package vn.io.arda.shared.event.tenant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.io.arda.shared.event.DomainEvent;

import java.time.Instant;

/**
 * Domain event published when a tenant's status changes
 * (ACTIVE/INACTIVE/SUSPENDED/TRIAL).
 * Triggers a notification to PLATFORM_ADMIN users.
 *
 * @since 0.0.1
 */
@Getter
@NoArgsConstructor
public class TenantStatusUpdatedEvent implements DomainEvent {

  private final String eventType = "TENANT_STATUS_UPDATED";
  private String tenantKey;
  private String status;
  private Instant occurredAt;

  @JsonCreator
  public TenantStatusUpdatedEvent(
      @JsonProperty("tenantKey") String tenantKey,
      @JsonProperty("status") String status) {
    this.tenantKey = tenantKey;
    this.status = status;
    this.occurredAt = Instant.now();
  }
}
