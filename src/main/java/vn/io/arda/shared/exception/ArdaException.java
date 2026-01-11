package vn.io.arda.shared.exception;

/**
 * Base runtime exception for all Arda platform exceptions.
 *
 * @since 0.0.1
 */
public class ArdaException extends RuntimeException {

    public ArdaException(String message) {
        super(message);
    }

    public ArdaException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArdaException(Throwable cause) {
        super(cause);
    }
}
