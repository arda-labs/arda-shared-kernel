package vn.io.arda.shared.multitenant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import vn.io.arda.shared.exception.TenantNotFoundException;
import vn.io.arda.shared.multitenant.dto.TenantInfo;
import vn.io.arda.shared.multitenant.model.TenantDataSourceConfig;
import vn.io.arda.shared.multitenant.properties.MultiTenancyProperties;

import java.util.List;

/**
 * Implementation of TenantMetadataService that fetches tenant information
 * from the central platform service via REST API.
 *
 * @since 0.0.1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CentralPlatformTenantService implements TenantMetadataService {

    private final MultiTenancyProperties properties;
    private final RestClient restClient;

    public CentralPlatformTenantService(MultiTenancyProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getCentralPlatformUrl())
                .build();
    }

    @Override
    public TenantDataSourceConfig getTenantDataSourceConfig(String tenantId) {
        log.debug("Fetching datasource config for tenant: {}", tenantId);

        try {
            TenantDataSourceConfig config = restClient.get()
                    .uri("/api/tenants/{tenantId}/datasource", tenantId)
                    .retrieve()
                    .body(TenantDataSourceConfig.class);

            if (config == null) {
                throw new TenantNotFoundException(tenantId);
            }

            log.debug("Retrieved datasource config for tenant {}: dbType={}",
                    tenantId, config.getDbType());
            return config;

        } catch (Exception e) {
            log.error("Failed to fetch datasource config for tenant: {}", tenantId, e);
            throw new TenantNotFoundException(tenantId, e);
        }
    }

    @Override
    public boolean tenantExists(String tenantId) {
        try {
            Boolean exists = restClient.get()
                    .uri("/api/tenants/{tenantId}/exists", tenantId)
                    .retrieve()
                    .body(Boolean.class);

            return Boolean.TRUE.equals(exists);

        } catch (Exception e) {
            log.error("Error checking tenant existence: {}", tenantId, e);
            return false;
        }
    }

    /**
     * Retrieves all active tenants from the central platform.
     * Used for batch operations like database migrations.
     *
     * @return list of all active tenants
     */
    public List<TenantInfo> getAllActiveTenants() {
        log.debug("Fetching all active tenants from central platform");

        try {
            List<TenantInfo> tenants = restClient.get()
                    .uri("/api/tenants/active")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            log.info("Retrieved {} active tenants", tenants != null ? tenants.size() : 0);
            return tenants != null ? tenants : List.of();

        } catch (Exception e) {
            log.error("Failed to fetch active tenants from central platform", e);
            return List.of();
        }
    }
}
