package vn.io.arda.shared.exception;

/**
 * Exception thrown when a user is not authorized to perform an operation.
 *
 * @since 0.0.1
 */
public class UnauthorizedException extends ArdaException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
