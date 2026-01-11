package vn.io.arda.shared.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for security features.
 *
 * @since 0.0.1
 */
@Data
@ConfigurationProperties(prefix = "arda.shared.security")
public class SecurityProperties {

    /**
     * Enable or disable security features.
     */
    private boolean enabled = true;

    /**
     * JWT configuration.
     */
    private JwtConfig jwt = new JwtConfig();

    /**
     * CORS configuration.
     */
    private CorsConfig cors = new CorsConfig();

    @Data
    public static class JwtConfig {
        /**
         * JWT issuer URI (Keycloak realm URL).
         */
        private String issuerUri = "http://localhost:8081/realms/arda";

        /**
         * JWK Set URI for token validation.
         */
        private String jwkSetUri;

        /**
         * JWT claim name for username.
         */
        private String usernameClaim = "preferred_username";

        /**
         * JWT claim name for roles.
         */
        private String rolesClaim = "realm_access.roles";

        /**
         * JWT claim name for tenant ID.
         */
        private String tenantClaim = "tenant_id";
    }

    @Data
    public static class CorsConfig {
        /**
         * Allowed origins for CORS.
         */
        private List<String> allowedOrigins = new ArrayList<>(List.of("*"));

        /**
         * Allowed methods for CORS.
         */
        private List<String> allowedMethods = new ArrayList<>(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        /**
         * Allowed headers for CORS.
         */
        private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

        /**
         * Max age for CORS preflight cache in seconds.
         */
        private long maxAge = 3600;
    }
}
