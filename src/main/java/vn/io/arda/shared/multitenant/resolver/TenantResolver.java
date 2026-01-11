package vn.io.arda.shared.multitenant.resolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Interface for resolving tenant ID from HTTP requests.
 *
 * @since 0.0.1
 */
public interface TenantResolver {

    /**
     * Resolves the tenant ID from the given HTTP request.
     *
     * @param request the HTTP request
     * @return Optional containing the tenant ID, or empty if not resolved
     */
    Optional<String> resolve(HttpServletRequest request);
}
