package vn.io.arda.shared.exception;

/**
 * Exception thrown when a tenant is not found or does not exist.
 *
 * @since 0.0.1
 */
public class TenantNotFoundException extends ArdaException {

    private final String tenantId;

    public TenantNotFoundException(String tenantId) {
        super("Tenant not found: " + tenantId);
        this.tenantId = tenantId;
    }

    public TenantNotFoundException(String tenantId, Throwable cause) {
        super("Tenant not found: " + tenantId, cause);
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
