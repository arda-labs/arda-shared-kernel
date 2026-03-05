package vn.io.arda.shared.event.tenant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.io.arda.shared.event.DomainEvent;

import java.time.Instant;

/**
 * Domain event published when a tenant is deleted.
 * Triggers Keycloak realm removal and tenant database drop.
 *
 * @since 0.0.1
 */
@Getter
@NoArgsConstructor
public class TenantDeletedEvent implements DomainEvent {

  private final String eventType = "TENANT_DELETED";
  private String tenantKey;
  private Instant occurredAt;

  @JsonCreator
  public TenantDeletedEvent(
      @JsonProperty("tenantKey") String tenantKey) {
    this.tenantKey = tenantKey;
    this.occurredAt = Instant.now();
  }
}
