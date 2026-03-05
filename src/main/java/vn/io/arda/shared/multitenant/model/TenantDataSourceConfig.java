package vn.io.arda.shared.multitenant.model;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @JsonAlias("tenantKey")
    private String tenantId;
    private String jdbcUrl;
    @JsonAlias("dbUsername")
    private String username;
    @JsonAlias("dbPassword")
    private String password;
    private String driverClassName;

    /**
     * Auto-detects driver class name from JDBC URL if not set.
     */
    public String getDriverClassName() {
        if (driverClassName == null && jdbcUrl != null) {
            if (jdbcUrl.startsWith("jdbc:postgresql")) {
                return "org.postgresql.Driver";
            }
        }
        return driverClassName;
    }

}
