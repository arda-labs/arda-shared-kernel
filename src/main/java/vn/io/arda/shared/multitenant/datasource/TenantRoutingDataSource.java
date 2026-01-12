package vn.io.arda.shared.multitenant.datasource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import vn.io.arda.shared.multitenant.context.TenantContext;
import vn.io.arda.shared.multitenant.model.TenantDataSourceConfig;
import vn.io.arda.shared.multitenant.service.TenantMetadataService;

import javax.sql.DataSource;

/**
 * Dynamic routing DataSource that routes to different databases based on current tenant context.
 *
 * <p>This class extends Spring's AbstractRoutingDataSource to provide tenant-aware database routing.
 * It uses TenantContext to determine which database to connect to for each request.</p>
 *
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final TenantDataSourceCache dataSourceCache;
    private final TenantMetadataService tenantMetadataService;

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getTenantId().orElse(null);
    }

    @Override
    protected DataSource determineTargetDataSource() {
        // CRITICAL: Multi-tenant SaaS requires tenant context for ALL database operations
        // Rejecting requests without tenant prevents data leakage and ensures proper isolation
        String tenantId = TenantContext.getTenantId()
                .orElseThrow(() -> new vn.io.arda.shared.exception.InvalidTenantContextException(
                        "Tenant context is required but not set. Please provide X-Tenant-ID header or valid JWT token with tenant claim."));

        try {
            // Get tenant configuration from central platform
            TenantDataSourceConfig config = tenantMetadataService.getTenantDataSourceConfig(tenantId);

            // Get or create DataSource from cache
            DataSource dataSource = dataSourceCache.getOrCreate(tenantId, config);

            log.trace("Routing to database for tenant: {}", tenantId);
            return dataSource;

        } catch (vn.io.arda.shared.exception.TenantNotFoundException e) {
            log.error("Tenant not found: {}", tenantId, e);
            throw e;
        } catch (Exception e) {
            log.error("Error determining target datasource for tenant: {}", tenantId, e);
            throw new RuntimeException("Failed to route to tenant database: " + tenantId, e);
        }
    }

    /**
     * Evicts a tenant's DataSource from cache.
     * Useful when tenant database configuration changes.
     *
     * @param tenantId the tenant ID
     */
    public void evictTenantDataSource(String tenantId) {
        dataSourceCache.evict(tenantId);
    }

    /**
     * Clears all cached DataSources.
     * Useful for maintenance or configuration reload.
     */
    public void clearAllDataSources() {
        dataSourceCache.clear();
    }
}
