package vn.io.arda.shared.multitenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing tenant information.
 *
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantInfo {

    /**
     * Unique tenant identifier.
     */
    private String tenantId;

    /**
     * Tenant name.
     */
    private String name;

    /**
     * Whether the tenant is active.
     */
    private boolean active;

    /**
     * Database connection information.
     */
    private String jdbcUrl;
    private String username;
    private String password;
    private String dbType;
}
