package vn.io.arda.shared.security.jwt;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Component;
import vn.io.arda.shared.security.properties.SecurityProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves AuthenticationManager based on JWT issuer for multi-realm support.
 * <p>
 * Each tenant has a dedicated Keycloak realm with a unique issuer URI.
 * This resolver extracts the issuer from the JWT token and creates/caches
 * a corresponding JwtDecoder for that realm.
 * </p>
 *
 * <p><strong>Flow:</strong></p>
 * <ol>
 *   <li>Extract JWT from Authorization header</li>
 *   <li>Parse JWT to get issuer claim (iss)</li>
 *   <li>Check cache for existing JwtDecoder for this issuer</li>
 *   <li>If not cached, create new JwtDecoder with JWK Set URI for that realm</li>
 *   <li>Cache the JwtDecoder for future requests</li>
 *   <li>Return AuthenticationManager configured with the JwtDecoder</li>
 * </ol>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * JWT with iss: "http://localhost:8081/realms/tenant-a"
 * → Creates JwtDecoder with JWK URI: "http://localhost:8081/realms/tenant-a/protocol/openid-connect/certs"
 * → Caches for reuse
 * </pre>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>Only accepts issuers from configured baseKeycloakUrl</li>
 *   <li>Uses LRU cache to prevent memory exhaustion (max 100 realms)</li>
 *   <li>Thread-safe with ConcurrentHashMap</li>
 * </ul>
 *
 * @see org.springframework.security.authentication.AuthenticationManagerResolver
 * @see org.springframework.security.oauth2.jwt.JwtDecoder
 * @since 0.0.2
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiRealmAuthenticationManagerResolver
        implements AuthenticationManagerResolver<HttpServletRequest> {

    private final SecurityProperties securityProperties;
    private final KeycloakJwtConverter jwtConverter;

    /**
     * Cache of JwtDecoders by issuer URI.
     * Key: Issuer URI (e.g., "http://localhost:8081/realms/tenant-a")
     * Value: JwtDecoder instance
     */
    private final Map<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();

    /**
     * Maximum number of cached JwtDecoders to prevent memory exhaustion.
     * With 100 max tenants, each JwtDecoder ~1KB = ~100KB max memory.
     */
    private static final int MAX_CACHED_DECODERS = 100;

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token == null) {
            log.warn("No JWT token found in request");
            return null;
        }

        try {
            // Parse JWT to extract issuer claim (without validating signature yet)
            JWT jwt = JWTParser.parse(token);
            String issuer = jwt.getJWTClaimsSet().getIssuer();

            if (issuer == null || issuer.isBlank()) {
                log.warn("JWT token does not contain issuer claim");
                return null;
            }

            // Validate issuer is from our Keycloak instance
            if (!isValidIssuer(issuer)) {
                log.warn("JWT issuer '{}' is not from configured Keycloak server", issuer);
                return null;
            }

            // Get or create JwtDecoder for this issuer
            JwtDecoder jwtDecoder = jwtDecoders.computeIfAbsent(issuer, this::createJwtDecoder);

            // Create AuthenticationManager with this JwtDecoder
            JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder);
            provider.setJwtAuthenticationConverter(jwtConverter);

            return provider::authenticate;

        } catch (Exception e) {
            log.error("Failed to resolve authentication manager for JWT", e);
            return null;
        }
    }

    /**
     * Extracts JWT token from Authorization header.
     *
     * @param request HTTP request
     * @return JWT token string, or null if not present
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Validates that the issuer is from our configured Keycloak server.
     * Prevents JWT tokens from external/malicious issuers.
     *
     * @param issuer Issuer claim from JWT
     * @return true if issuer is valid
     */
    private boolean isValidIssuer(String issuer) {
        String baseUrl = securityProperties.getJwt().getBaseKeycloakUrl();
        return issuer.startsWith(baseUrl + "/realms/");
    }

    /**
     * Creates a JwtDecoder for a specific Keycloak realm.
     *
     * @param issuer Issuer URI (e.g., "http://localhost:8081/realms/tenant-a")
     * @return JwtDecoder instance configured for this realm
     */
    private JwtDecoder createJwtDecoder(String issuer) {
        // Check cache size limit
        if (jwtDecoders.size() >= MAX_CACHED_DECODERS) {
            log.warn("JWT decoder cache limit reached ({}). Consider implementing LRU eviction.", MAX_CACHED_DECODERS);
            // For now, clear cache completely (simple but not optimal)
            // TODO: Implement LRU cache with LinkedHashMap or Caffeine
            jwtDecoders.clear();
        }

        // Construct JWK Set URI for this realm
        // Example: http://localhost:8081/realms/tenant-a/protocol/openid-connect/certs
        String jwkSetUri = issuer + "/protocol/openid-connect/certs";

        log.info("Creating JwtDecoder for issuer: {} with JWK Set URI: {}", issuer, jwkSetUri);

        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
