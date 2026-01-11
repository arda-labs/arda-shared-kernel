package vn.io.arda.shared.multitenant.datasource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;
import vn.io.arda.shared.multitenant.context.TenantContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hibernate MultiTenantConnectionProvider implementation that integrates with
 * TenantRoutingDataSource to provide tenant-specific database connections.
 * <p>
 * This provider is used by Hibernate's multi-tenancy strategy to obtain connections
 * for specific tenants. It delegates connection management to the routing DataSource
 * and uses TenantContext to determine the current tenant.
 * </p>
 *
 * <p>Usage in Hibernate configuration:</p>
 * <pre>
 * spring:
 *   jpa:
 *     properties:
 *       hibernate:
 *         multiTenancy: DATABASE
 *         multi_tenant_connection_provider: vn.io.arda.shared.multitenant.datasource.TenantConnectionProvider
 * </pre>
 *
 * @author Arda Development Team
 * @see TenantRoutingDataSource
 * @see TenantContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class TenantConnectionProvider implements MultiTenantConnectionProvider {

    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        log.trace("Getting default connection");
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        log.trace("Releasing default connection");
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        String tenantId = tenantIdentifier != null ? tenantIdentifier.toString() : null;
        log.debug("Getting connection for tenant: {}", tenantId);

        // Set tenant context before getting connection
        TenantContext.setCurrentTenant(tenantId);

        try {
            Connection connection = dataSource.getConnection();
            log.trace("Connection obtained for tenant: {}", tenantId);
            return connection;
        } catch (SQLException e) {
            log.error("Failed to get connection for tenant: {}", tenantId, e);
            throw e;
        }
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        String tenantId = tenantIdentifier != null ? tenantIdentifier.toString() : null;
        log.debug("Releasing connection for tenant: {}", tenantId);

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.trace("Connection released for tenant: {}", tenantId);
            }
        } catch (SQLException e) {
            log.error("Failed to release connection for tenant: {}", tenantId, e);
            throw e;
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        // Enable aggressive connection release for better resource management
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType)
                || DataSource.class.isAssignableFrom(unwrapType);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        if (MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType)) {
            return (T) this;
        } else if (DataSource.class.isAssignableFrom(unwrapType)) {
            return (T) dataSource;
        }
        throw new IllegalArgumentException("Cannot unwrap to " + unwrapType);
    }
}
