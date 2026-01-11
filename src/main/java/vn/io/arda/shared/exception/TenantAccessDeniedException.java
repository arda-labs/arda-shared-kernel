package vn.io.arda.shared.exception;

/**
 * Exception thrown when a user attempts to access a tenant they don't have permission for.
 *
 * @since 0.0.1
 */
public class TenantAccessDeniedException extends ArdaException {

    private final String tenantId;
    private final String userId;

    public TenantAccessDeniedException(String tenantId, String userId) {
        super(String.format("User '%s' does not have access to tenant '%s'", userId, tenantId));
        this.tenantId = tenantId;
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }
}
