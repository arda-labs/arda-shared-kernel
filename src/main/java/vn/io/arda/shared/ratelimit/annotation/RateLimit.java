package vn.io.arda.shared.ratelimit.annotation;

import java.lang.annotation.*;

/**
 * Annotation for method-level rate limiting.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @RateLimit(key = "api:search", limit = 100, windowSeconds = 60)
 * public void processRequest() {
 *     // ...
 * }
 * }</pre>
 *
 * @since 0.0.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * Unique key for the rate limit bucket (e.g., "api:endpoint").
     * The tenant ID will be prepended automatically.
     */
    String key();

    /**
     * Maximum number of requests allowed in the time window.
     */
    int limit() default 100;

    /**
     * Time window in seconds for the rate limit.
     */
    int windowSeconds() default 60;
}
