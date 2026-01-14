package vn.io.arda.shared.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.io.arda.shared.security.jwt.JwtUtils;

import java.io.IOException;

/**
 * Security filter that validates JWT tenant_id claim matches X-Tenant-ID header.
 * <p>
 * This filter prevents tenant isolation bypass attacks where a user could send
 * a JWT from tenant-a with X-Tenant-ID header for tenant-b to access another
 * tenant's data.
 * </p>
 *
 * <p><strong>Execution Order:</strong></p>
 * <ol>
 *   <li>Spring Security validates JWT signature and expiration</li>
 *   <li><strong>THIS FILTER</strong> validates tenant_id in JWT matches X-Tenant-ID header</li>
 *   <li>TenantContextFilter sets tenant context from X-Tenant-ID</li>
 *   <li>TenantRoutingDataSource routes queries to tenant database</li>
 * </ol>
 *
 * <p><strong>Security Flow:</strong></p>
 * <pre>
 * Request with:
 *   Authorization: Bearer {JWT with tenant_id: "tenant-a"}
 *   X-Tenant-ID: tenant-b
 *
 * → Filter detects mismatch
 * → Returns 403 Forbidden
 * → Prevents access to tenant-b's data
 * </pre>
 *
 * <p><strong>Order:</strong></p>
 * <p>This filter must run AFTER Spring Security (to have authenticated JWT)
 * but BEFORE TenantContextFilter (to prevent setting wrong tenant context).</p>
 *
 * @see vn.io.arda.shared.multitenant.filter.TenantContextFilter
 * @see vn.io.arda.shared.security.jwt.MultiRealmAuthenticationManagerResolver
 * @since 0.0.2
 */
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // After Spring Security, before TenantContextFilter
public class TenantJwtValidationFilter implements Filter {

    private final JwtUtils jwtUtils;
    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip validation for public endpoints (health, error, etc.)
        if (isPublicEndpoint(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // Get authenticated JWT from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            // Extract tenant_id from JWT claims
            String jwtTenantId = jwtUtils.extractTenantId(jwt);

            // Get X-Tenant-ID header
            String headerTenantId = httpRequest.getHeader(TENANT_HEADER);

            // Validate tenant_id match
            if (!validateTenantMatch(jwtTenantId, headerTenantId, httpRequest)) {
                log.warn("Tenant mismatch detected - JWT tenant_id: '{}', Header X-Tenant-ID: '{}', URI: {}",
                        jwtTenantId, headerTenantId, httpRequest.getRequestURI());

                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                        "{\"error\":\"Forbidden\",\"message\":\"Tenant ID in JWT does not match X-Tenant-ID header\"}"
                );
                return;
            }

            log.trace("Tenant validation passed - tenant_id: {}", jwtTenantId);
        }

        chain.doFilter(request, response);
    }

    /**
     * Validates that JWT tenant_id matches X-Tenant-ID header.
     *
     * @param jwtTenantId Tenant ID from JWT claim
     * @param headerTenantId Tenant ID from X-Tenant-ID header
     * @param request HTTP request for logging
     * @return true if valid
     */
    private boolean validateTenantMatch(String jwtTenantId, String headerTenantId, HttpServletRequest request) {
        // If JWT doesn't have tenant_id claim, allow (might be super admin from master realm)
        if (jwtTenantId == null || jwtTenantId.isBlank()) {
            log.debug("JWT does not contain tenant_id claim - allowing request to {}", request.getRequestURI());
            return true;
        }

        // If header is missing but JWT has tenant_id, reject
        if (headerTenantId == null || headerTenantId.isBlank()) {
            log.warn("X-Tenant-ID header missing but JWT contains tenant_id: {}", jwtTenantId);
            return false;
        }

        // Both present - must match exactly
        return jwtTenantId.equals(headerTenantId);
    }

    /**
     * Checks if the request is for a public endpoint that doesn't require tenant validation.
     *
     * @param request HTTP request
     * @return true if public endpoint
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator/") ||
                uri.startsWith("/health") ||
                uri.startsWith("/error") ||
                uri.startsWith("/api/v1/public/");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("TenantJwtValidationFilter initialized - Tenant ID mismatch protection enabled");
    }

    @Override
    public void destroy() {
        log.info("TenantJwtValidationFilter destroyed");
    }
}
