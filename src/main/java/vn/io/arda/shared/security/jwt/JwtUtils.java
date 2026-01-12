package vn.io.arda.shared.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import vn.io.arda.shared.security.properties.SecurityProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for extracting claims from VALIDATED JWT tokens.
 *
 * <p><strong>CRITICAL SECURITY NOTE:</strong></p>
 * <ul>
 *   <li>This class does NOT validate JWT signatures or expiration</li>
 *   <li>JWT validation is handled by Spring Security's {@code JwtDecoder}</li>
 *   <li>All methods assume the JWT has already been validated by OAuth2 Resource Server</li>
 *   <li>Never use this class with untrusted/unvalidated JWT strings</li>
 * </ul>
 *
 * <p><strong>JWT Validation Flow:</strong></p>
 * <pre>
 * 1. Client sends request with Authorization: Bearer [token]
 * 2. Spring Security {@code JwtDecoder} validates:
 *    - Signature (using public key from Keycloak)
 *    - Expiration (exp claim)
 *    - Issuer (iss claim)
 *    - Not Before (nbf claim)
 * 3. Valid JWT is converted to {@code Authentication} by {@link KeycloakJwtConverter}
 * 4. THIS class extracts claims from the VALIDATED JWT
 * </pre>
 *
 * <p><strong>Keycloak Token Structure:</strong></p>
 * <pre>
 * {
 *   "sub": "user-uuid",                    // User ID
 *   "preferred_username": "john.doe",      // Username
 *   "tenant_id": "tenant-123",             // Tenant claim (custom)
 *   "realm_access": {
 *     "roles": ["admin", "user"]           // Keycloak roles
 *   },
 *   "exp": 1641234567,                     // Expiration
 *   "iss": "http://keycloak:8080/realms/arda" // Issuer
 * }
 * </pre>
 *
 * @see org.springframework.security.oauth2.jwt.JwtDecoder
 * @see KeycloakJwtConverter
 * @see org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
 * @since 0.0.1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final SecurityProperties securityProperties;

    /**
     * Extracts username from JWT token.
     *
     * @param jwt the JWT token
     * @return the username
     */
    public String extractUsername(Jwt jwt) {
        String claim = securityProperties.getJwt().getUsernameClaim();
        return jwt.getClaimAsString(claim);
    }

    /**
     * Extracts roles from JWT token.
     * Keycloak stores roles in realm_access.roles by default.
     *
     * @param jwt the JWT token
     * @return list of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Jwt jwt) {
        try {
            // Keycloak format: realm_access.roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                Object roles = realmAccess.get("roles");
                if (roles instanceof List) {
                    return (List<String>) roles;
                }
            }

            // Fallback: check direct roles claim
            List<String> roles = jwt.getClaim("roles");
            return roles != null ? roles : new ArrayList<>();

        } catch (Exception e) {
            log.warn("Error extracting roles from JWT", e);
            return new ArrayList<>();
        }
    }

    /**
     * Extracts tenant ID from JWT token.
     *
     * @param jwt the JWT token
     * @return the tenant ID, or null if not present
     */
    public String extractTenantId(Jwt jwt) {
        String claim = securityProperties.getJwt().getTenantClaim();
        return jwt.getClaimAsString(claim);
    }

    /**
     * Extracts user ID from JWT token.
     *
     * @param jwt the JWT token
     * @return the user ID (subject)
     */
    public String extractUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    /**
     * Checks if JWT token has a specific role.
     *
     * @param jwt the JWT token
     * @param role the role to check
     * @return true if user has the role
     */
    public boolean hasRole(Jwt jwt, String role) {
        return extractRoles(jwt).contains(role);
    }

    /**
     * Checks if JWT token has any of the specified roles.
     *
     * @param jwt the JWT token
     * @param roles the roles to check
     * @return true if user has any of the roles
     */
    public boolean hasAnyRole(Jwt jwt, String... roles) {
        List<String> userRoles = extractRoles(jwt);
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts all claims from JWT token as a map.
     *
     * @param jwt the JWT token
     * @return map of all claims
     */
    public Map<String, Object> extractAllClaims(Jwt jwt) {
        return jwt.getClaims();
    }
}
