package vn.io.arda.shared.multitenant.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import vn.io.arda.shared.multitenant.context.TenantContext;
import vn.io.arda.shared.multitenant.resolver.TenantResolver;

/**
 * HTTP interceptor that extracts tenant ID from requests and sets it in TenantContext.
 *
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantResolver tenantResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        tenantResolver.resolve(request).ifPresent(tenantId -> {
            TenantContext.setTenantId(tenantId);
            log.debug("Tenant context set: {} for request: {} {}",
                    tenantId, request.getMethod(), request.getRequestURI());
        });

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String tenantId = TenantContext.getTenantId().orElse(null);
        TenantContext.clear();

        if (tenantId != null) {
            log.trace("Tenant context cleared: {}", tenantId);
        }
    }
}
