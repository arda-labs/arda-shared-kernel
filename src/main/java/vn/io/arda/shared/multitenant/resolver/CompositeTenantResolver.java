package vn.io.arda.shared.multitenant.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Composite tenant resolver that tries multiple resolvers in order.
 * Returns the first successfully resolved tenant ID.
 *
 * @since 0.0.1
 */
@Slf4j
public class CompositeTenantResolver implements TenantResolver {

    private final List<TenantResolver> resolvers;

    public CompositeTenantResolver(List<TenantResolver> resolvers) {
        this.resolvers = resolvers;
    }

    @Override
    public Optional<String> resolve(HttpServletRequest request) {
        for (TenantResolver resolver : resolvers) {
            Optional<String> tenantId = resolver.resolve(request);
            if (tenantId.isPresent()) {
                log.trace("Tenant ID resolved by {}: {}",
                    resolver.getClass().getSimpleName(), tenantId.get());
                return tenantId;
            }
        }
        log.warn("No tenant ID resolved by any resolver");
        return Optional.empty();
    }
}
