package vn.io.arda.shared.multitenant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for a tenant's database connection.
 *
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDataSourceConfig {

    private String tenantId;
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private DatabaseType dbType;

    public enum DatabaseType {
        POSTGRESQL("org.postgresql.Driver"),
        ORACLE("oracle.jdbc.OracleDriver");

        private final String driverClass;

        DatabaseType(String driverClass) {
            this.driverClass = driverClass;
        }

        public String getDriverClass() {
            return driverClass;
        }

        public static DatabaseType fromJdbcUrl(String jdbcUrl) {
            if (jdbcUrl.startsWith("jdbc:postgresql")) {
                return POSTGRESQL;
            } else if (jdbcUrl.startsWith("jdbc:oracle")) {
                return ORACLE;
            }
            throw new IllegalArgumentException("Unsupported JDBC URL: " + jdbcUrl);
        }
    }

    /**
     * Auto-detects driver class name from JDBC URL if not set.
     */
    public String getDriverClassName() {
        if (driverClassName == null && jdbcUrl != null) {
            DatabaseType type = DatabaseType.fromJdbcUrl(jdbcUrl);
            return type.getDriverClass();
        }
        return driverClassName;
    }
}
