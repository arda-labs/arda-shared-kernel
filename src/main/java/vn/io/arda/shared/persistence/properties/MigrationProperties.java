package vn.io.arda.shared.persistence.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for database migration.
 *
 * @since 0.0.1
 */
@Data
@ConfigurationProperties(prefix = "arda.shared.migration")
public class MigrationProperties {

    /**
     * Enable or disable auto-migration on startup.
     */
    private boolean autoMigrate = true;

    /**
     * Fail application startup if migration fails.
     */
    private boolean failOnError = true;

    /**
     * Run migrations in parallel for multiple tenants.
     */
    private boolean parallel = false;
}
