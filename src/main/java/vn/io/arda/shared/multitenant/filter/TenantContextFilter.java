package vn.io.arda.shared.multitenant.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.arda.shared.multitenant.context.TenantContext;
import vn.io.arda.shared.multitenant.resolver.TenantResolver;

import java.io.IOException;

/**
 * Servlet filter that ensures TenantContext is ALWAYS cleared, even on exceptions.
 * This prevents tenant context leakage in servlet container thread pools.
 *
 * <p><strong>CRITICAL SECURITY:</strong> This filter uses try-finally to guarantee
 * that {@link TenantContext#clear()} is called even when exceptions occur. Without this,
 * thread pool reuse could cause Tenant A to access Tenant B's data.</p>
 *
 * <p>Execution order:</p>
 * <ol>
 *   <li>Extract tenant ID from request using {@link TenantResolver}</li>
 *   <li>Set tenant context in ThreadLocal via {@link TenantContext#setTenantId(String)}</li>
 *   <li>Process request (even if it throws exception)</li>
 *   <li><strong>ALWAYS</strong> clear tenant context in finally block</li>
 * </ol>
 *
 * <p>This filter must be registered with {@code Ordered.HIGHEST_PRECEDENCE} to ensure
 * it runs before any other filter that might access tenant data.</p>
 *
 * @see TenantContext
 * @see TenantResolver
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
public class TenantContextFilter implements Filter {

    private final TenantResolver tenantResolver;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest httpRequest) {
                tenantResolver.resolve(httpRequest).ifPresent(tenantId -> {
                    TenantContext.setTenantId(tenantId);
                    log.debug("Tenant context set: {} for request: {} {}",
                            tenantId, httpRequest.getMethod(), httpRequest.getRequestURI());
                });
            }
            chain.doFilter(request, response);
        } finally {
            // CRITICAL: Always clear context, even on exception
            // This prevents tenant data leakage in thread pool reuse
            String tenantId = TenantContext.getCurrentTenant();
            TenantContext.clear();
            if (tenantId != null) {
                log.trace("Tenant context cleared: {}", tenantId);
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("TenantContextFilter initialized - Thread-safe tenant context management enabled");
    }

    @Override
    public void destroy() {
        log.info("TenantContextFilter destroyed");
    }
}
