package vn.io.arda.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for database migration features.
 *
 * @since 0.0.1
 */
@Data
@ConfigurationProperties(prefix = "arda.shared.migration")
public class ArdaMigrationProperties {

    /**
     * Enable or disable automatic migration on startup.
     */
    private boolean autoMigrate = false;

    /**
     * Path to the Liquibase changelog file.
     */
    private String changelogPath = "db/changelog/db.changelog-master.yaml";

    /**
     * Liquibase contexts to run (comma-separated).
     */
    private String contexts = "";

    /**
     * Fail application startup if migration fails.
     */
    private boolean failOnError = true;
}
