package vn.io.arda.shared.event.tenant;

import lombok.Getter;
import vn.io.arda.shared.event.DomainEvent;

import java.time.Instant;

/**
 * Domain event published when a new tenant is created.
 * This event triggers Keycloak realm provisioning in downstream services.
 *
 * @since 0.0.1
 */
@Getter
public class TenantCreatedEvent implements DomainEvent {

    private final String tenantKey;
    private final String displayName;
    private final String dbType;
    private final Instant occurredAt;

    public TenantCreatedEvent(String tenantKey, String displayName, String dbType) {
        this.tenantKey = tenantKey;
        this.displayName = displayName;
        this.dbType = dbType;
        this.occurredAt = Instant.now();
    }
}
