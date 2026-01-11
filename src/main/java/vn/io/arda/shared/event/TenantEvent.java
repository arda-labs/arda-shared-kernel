package vn.io.arda.shared.event;

import lombok.Data;
import vn.io.arda.shared.multitenant.context.TenantContext;

import java.time.Instant;

/**
 * Base class for tenant-aware domain events.
 * Automatically captures tenant ID from context.
 *
 * @since 0.0.1
 */
@Data
public abstract class TenantEvent implements DomainEvent {

    private final String tenantId;
    private final Instant occurredAt;

    protected TenantEvent() {
        this.tenantId = TenantContext.getRequiredTenantId();
        this.occurredAt = Instant.now();
    }

    protected TenantEvent(String tenantId) {
        this.tenantId = tenantId;
        this.occurredAt = Instant.now();
    }
}
