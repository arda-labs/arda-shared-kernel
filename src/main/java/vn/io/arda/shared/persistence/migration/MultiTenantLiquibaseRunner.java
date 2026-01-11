package vn.io.arda.shared.persistence.migration;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.io.arda.shared.config.properties.ArdaMigrationProperties;
import vn.io.arda.shared.multitenant.dto.TenantInfo;
import vn.io.arda.shared.multitenant.service.CentralPlatformTenantService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * Application runner that automatically runs Liquibase migrations for all tenants on startup.
 * <p>
 * This runner queries all active tenants from the Central Platform service and executes
 * Liquibase changelog on each tenant's database. It only runs when multi-tenancy and
 * auto-migration are enabled.
 * </p>
 *
 * <p>Configuration example:</p>
 * <pre>
 * arda:
 *   shared:
 *     multi-tenancy:
 *       enabled: true
 *     migration:
 *       auto-migrate: true
 *       changelog-path: db/changelog/db.changelog-master.yaml
 * </pre>
 *
 * @author Arda Development Team
 * @see CentralPlatformTenantService
 * @see ArdaMigrationProperties
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "arda.shared.migration", name = "auto-migrate", havingValue = "true")
public class MultiTenantLiquibaseRunner implements ApplicationRunner {

    private final CentralPlatformTenantService tenantService;
    private final ArdaMigrationProperties migrationProperties;
    private final DataSource routingDataSource;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting multi-tenant Liquibase migration...");

        try {
            List<TenantInfo> tenants = tenantService.getAllActiveTenants();
            log.info("Found {} active tenants to migrate", tenants.size());

            int successCount = 0;
            int failureCount = 0;

            for (TenantInfo tenant : tenants) {
                try {
                    runMigrationForTenant(tenant);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to migrate tenant: {}", tenant.getTenantId(), e);
                    failureCount++;

                    if (migrationProperties.isFailOnError()) {
                        throw new RuntimeException("Migration failed for tenant: " + tenant.getTenantId(), e);
                    }
                }
            }

            log.info("Multi-tenant migration completed. Success: {}, Failed: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("Multi-tenant migration failed", e);
            if (migrationProperties.isFailOnError()) {
                throw new RuntimeException("Multi-tenant migration failed", e);
            }
        }
    }

    /**
     * Runs Liquibase migration for a specific tenant.
     *
     * @param tenant the tenant information
     * @throws LiquibaseException if migration fails
     */
    private void runMigrationForTenant(TenantInfo tenant) throws Exception {
        log.info("Running migration for tenant: {} ({})", tenant.getTenantId(), tenant.getName());

        try (Connection connection = routingDataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    migrationProperties.getChangelogPath(),
                    new ClassLoaderResourceAccessor(),
                    database
            );

            // Run update
            liquibase.update(migrationProperties.getContexts());

            log.info("Migration completed successfully for tenant: {}", tenant.getTenantId());

        } catch (Exception e) {
            log.error("Migration failed for tenant: {}", tenant.getTenantId(), e);
            throw e;
        }
    }
}
