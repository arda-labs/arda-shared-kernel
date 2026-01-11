package vn.io.arda.shared.multitenant.context;

import vn.io.arda.shared.exception.InvalidTenantContextException;

import java.util.Optional;

/**
 * Thread-local storage for current tenant context.
 * Each thread can have its own tenant ID associated with it.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * TenantContext.setTenantId("tenant-123");
 * String tenantId = TenantContext.getRequiredTenantId();
 * TenantContext.clear();
 * }</pre>
 *
 * @since 0.0.1
 */
public final class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    private TenantContext() {
        // Utility class
    }

    /**
     * Sets the current tenant ID for this thread.
     *
     * @param tenantId the tenant ID to set
     */
    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Alias for setTenantId() - sets the current tenant for this thread.
     *
     * @param tenantId the tenant ID to set
     */
    public static void setCurrentTenant(String tenantId) {
        setTenantId(tenantId);
    }

    /**
     * Gets the current tenant ID for this thread.
     *
     * @return Optional containing the tenant ID, or empty if not set
     */
    public static Optional<String> getTenantId() {
        return Optional.ofNullable(currentTenant.get());
    }

    /**
     * Alias for getTenantId() - gets the current tenant ID (nullable).
     *
     * @return the current tenant ID or null if not set
     */
    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    /**
     * Gets the current tenant ID or throws an exception if not set.
     *
     * @return the current tenant ID
     * @throws InvalidTenantContextException if tenant context is not set
     */
    public static String getRequiredTenantId() {
        return getTenantId()
                .orElseThrow(() -> new InvalidTenantContextException(
                        "No tenant context available. Ensure X-Tenant-ID header is provided or JWT contains tenant claim."));
    }

    /**
     * Clears the tenant context for this thread.
     * Should be called in a finally block or using try-with-resources.
     */
    public static void clear() {
        currentTenant.remove();
    }
}
