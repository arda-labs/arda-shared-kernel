package vn.io.arda.shared.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import vn.io.arda.shared.multitenant.context.TenantContext;

import java.util.Collection;

/**
 * Tenant-aware cache manager that prefixes all cache keys with tenant ID.
 *
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
public class TenantAwareCacheManager implements CacheManager {

    private final CacheManager delegate;

    @Override
    @Nullable
    public Cache getCache(String name) {
        String tenantId = TenantContext.getTenantId().orElse("default");
        String tenantAwareName = tenantId + ":" + name;

        log.trace("Getting cache with tenant-aware name: {}", tenantAwareName);
        return delegate.getCache(tenantAwareName);
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
