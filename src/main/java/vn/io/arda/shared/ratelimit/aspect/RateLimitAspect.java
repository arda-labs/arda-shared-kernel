package vn.io.arda.shared.ratelimit.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import vn.io.arda.shared.multitenant.context.TenantContext;
import vn.io.arda.shared.ratelimit.annotation.RateLimit;
import vn.io.arda.shared.ratelimit.exception.RateLimitExceededException;
import vn.io.arda.shared.ratelimit.strategy.RateLimitStrategy;

import java.lang.reflect.Method;

/**
 * AspectJ aspect that intercepts methods annotated with {@link RateLimit} and enforces
 * rate limiting policies.
 * <p>
 * This aspect extracts rate limit configuration from the annotation and delegates to
 * {@link RateLimitStrategy} to check if the request should be allowed. If the limit is
 * exceeded, it throws {@link RateLimitExceededException}.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>
 * &#64;RestController
 * public class ApiController {
 *     &#64;RateLimit(key = "api:search", limit = 100, windowSeconds = 60)
 *     &#64;GetMapping("/search")
 *     public List&lt;Result&gt; search() {
 *         // This method is limited to 100 calls per minute
 *     }
 * }
 * </pre>
 *
 * @author Arda Development Team
 * @see RateLimit
 * @see RateLimitStrategy
 * @see RateLimitExceededException
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitStrategy rateLimitStrategy;

    /**
     * Around advice that intercepts methods annotated with @RateLimit.
     * Checks rate limit before allowing method execution.
     *
     * @param joinPoint the join point representing the intercepted method
     * @return the result of the method execution
     * @throws Throwable if method execution fails or rate limit is exceeded
     */
    @Around("@annotation(vn.io.arda.shared.ratelimit.annotation.RateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        if (rateLimit == null) {
            log.warn("RateLimit annotation not found on method: {}", method.getName());
            return joinPoint.proceed();
        }

        // Build rate limit key
        String key = buildRateLimitKey(rateLimit.key());

        log.debug("Checking rate limit for key: {} (limit: {}, window: {}s)",
                key, rateLimit.limit(), rateLimit.windowSeconds());

        // Check rate limit
        boolean allowed = rateLimitStrategy.isAllowed(
                key,
                rateLimit.limit(),
                rateLimit.windowSeconds()
        );

        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}", key);
            throw new RateLimitExceededException(rateLimit.limit(), key);
        }

        log.trace("Rate limit check passed for key: {}", key);
        return joinPoint.proceed();
    }

    /**
     * Builds the complete rate limit key by combining the base key with tenant context.
     * Format: {tenantId}:{baseKey}
     *
     * @param baseKey the base key from the annotation
     * @return the complete rate limit key
     */
    private String buildRateLimitKey(String baseKey) {
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId != null && !tenantId.isBlank()) {
            return tenantId + ":" + baseKey;
        }

        return baseKey;
    }
}
