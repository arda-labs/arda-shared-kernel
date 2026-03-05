package vn.io.arda.shared.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Internal JWT verification.
 * Internal JWTs are signed by APISIX Gateway and verified by backend services.
 *
 * @since 0.1.0
 */
@Data
@ConfigurationProperties(prefix = "arda.shared.internal-jwt")
public class InternalJwtProperties {

  /**
   * Enable Internal JWT mode (Gateway AuthN).
   * When true, services verify Internal JWTs from APISIX instead of Keycloak
   * JWTs.
   * When false, falls back to legacy MultiRealmAuthenticationManagerResolver.
   */
  private boolean enabled = false;

  /**
   * Path to the RSA public key file for verifying Internal JWTs.
   * Supports classpath: and file: prefixes.
   * Example: classpath:internal-jwt-public.pem
   */
  private String publicKeyLocation = "classpath:internal-jwt-public.pem";

  /**
   * Expected issuer claim in Internal JWTs.
   * Must match the issuer set by APISIX Lua signer.
   */
  private String issuer = "arda-gateway";

  /**
   * Header name containing the Internal JWT.
   */
  private String headerName = "X-Internal-Token";

  /**
   * Clock skew tolerance in seconds for expiration validation.
   */
  private int clockSkewSeconds = 30;
}
