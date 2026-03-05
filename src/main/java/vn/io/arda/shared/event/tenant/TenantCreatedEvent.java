package vn.io.arda.shared.event.tenant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.io.arda.shared.event.DomainEvent;

import java.time.Instant;

/**
 * Domain event published when a new tenant is created.
 * This event triggers Keycloak realm provisioning in downstream services.
 *
 * @since 0.0.1
 */
@Getter
@NoArgsConstructor
public class TenantCreatedEvent implements DomainEvent {

    private final String eventType = "TENANT_CREATED";
    private String tenantKey;
    private String displayName;
    private Instant occurredAt;

    @JsonCreator
    public TenantCreatedEvent(
            @JsonProperty("tenantKey") String tenantKey,
            @JsonProperty("displayName") String displayName) {
        this.tenantKey = tenantKey;
        this.displayName = displayName;
        this.occurredAt = Instant.now();
    }
}
