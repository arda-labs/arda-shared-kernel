package vn.io.arda.shared.multitenant.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Resolves tenant ID from HTTP header (default: X-Tenant-ID).
 *
 * @since 0.0.1
 */
@Slf4j
public class HeaderTenantResolver implements TenantResolver {

    private final String headerName;

    public HeaderTenantResolver(String headerName) {
        this.headerName = headerName;
    }

    public HeaderTenantResolver() {
        this("X-Tenant-ID");
    }

    @Override
    public Optional<String> resolve(HttpServletRequest request) {
        String tenantId = request.getHeader(headerName);
        if (tenantId != null && !tenantId.isBlank()) {
            log.trace("Resolved tenant ID from header {}: {}", headerName, tenantId);
            return Optional.of(tenantId);
        }
        log.trace("No tenant ID found in header: {}", headerName);
        return Optional.empty();
    }
}
