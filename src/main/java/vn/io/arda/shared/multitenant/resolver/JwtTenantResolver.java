package vn.io.arda.shared.multitenant.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Resolves tenant ID from JWT token claim.
 * Expects a "tenant_id" claim in the JWT token.
 *
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTenantResolver implements TenantResolver {

    private final String claimName;

    public JwtTenantResolver() {
        this("tenant_id");
    }

    @Override
    public Optional<String> resolve(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String tenantId = jwt.getClaimAsString(claimName);
            if (tenantId != null && !tenantId.isBlank()) {
                log.trace("Resolved tenant ID from JWT claim {}: {}", claimName, tenantId);
                return Optional.of(tenantId);
            }
        }

        log.trace("No tenant ID found in JWT claim: {}", claimName);
        return Optional.empty();
    }
}
