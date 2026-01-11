package vn.io.arda.shared.multitenant.context;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe holder for tenant context with auto-cleanup support.
 * Implements AutoCloseable for use in try-with-resources blocks.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * try (TenantContextHolder holder = new TenantContextHolder("tenant-123")) {
 *     // Tenant context is set
 *     doWork();
 * } // Automatically cleared
 * }</pre>
 *
 * @since 0.0.1
 */
@Slf4j
public class TenantContextHolder implements AutoCloseable {

    private final String previousTenantId;

    public TenantContextHolder(String tenantId) {
        this.previousTenantId = TenantContext.getTenantId().orElse(null);
        TenantContext.setTenantId(tenantId);
        log.trace("Tenant context set to: {}", tenantId);
    }

    @Override
    public void close() {
        if (previousTenantId != null) {
            TenantContext.setTenantId(previousTenantId);
            log.trace("Tenant context restored to: {}", previousTenantId);
        } else {
            TenantContext.clear();
            log.trace("Tenant context cleared");
        }
    }
}
