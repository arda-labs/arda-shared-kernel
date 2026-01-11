package vn.io.arda.shared.ratelimit.exception;

import vn.io.arda.shared.exception.ArdaException;

/**
 * Exception thrown when rate limit is exceeded.
 *
 * @since 0.0.1
 */
public class RateLimitExceededException extends ArdaException {

    private final int limit;
    private final String scope;

    public RateLimitExceededException(int limit, String scope) {
        super(String.format("Rate limit exceeded: %d requests per minute for scope '%s'", limit, scope));
        this.limit = limit;
        this.scope = scope;
    }

    public int getLimit() {
        return limit;
    }

    public String getScope() {
        return scope;
    }
}
