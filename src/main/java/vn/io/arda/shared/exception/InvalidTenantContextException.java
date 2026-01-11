package vn.io.arda.shared.exception;

/**
 * Exception thrown when tenant context is required but not available.
 *
 * @since 0.0.1
 */
public class InvalidTenantContextException extends ArdaException {

    public InvalidTenantContextException(String message) {
        super(message);
    }

    public InvalidTenantContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
