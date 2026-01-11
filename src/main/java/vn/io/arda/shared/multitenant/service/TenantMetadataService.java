package vn.io.arda.shared.multitenant.service;

import vn.io.arda.shared.multitenant.model.TenantDataSourceConfig;

/**
 * Service interface for retrieving tenant metadata and database configuration.
 *
 * @since 0.0.1
 */
public interface TenantMetadataService {

    /**
     * Gets the database configuration for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @return the tenant's database configuration
     * @throws vn.io.arda.shared.exception.TenantNotFoundException if tenant not found
     */
    TenantDataSourceConfig getTenantDataSourceConfig(String tenantId);

    /**
     * Checks if a tenant exists.
     *
     * @param tenantId the tenant ID
     * @return true if tenant exists, false otherwise
     */
    boolean tenantExists(String tenantId);
}
