package vn.io.arda.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import vn.io.arda.shared.security.filter.TenantJwtValidationFilter;
import vn.io.arda.shared.security.jwt.JwtUtils;
import vn.io.arda.shared.security.jwt.MultiRealmAuthenticationManagerResolver;
import vn.io.arda.shared.security.properties.SecurityProperties;

/**
 * Security configuration for Legacy Keycloak mode.
 * <p>
 * Activated when {@code arda.shared.internal-jwt.enabled=false} (default).
 * Each service validates Keycloak JWTs directly via multi-realm resolution.
 * </p>
 *
 * @deprecated Migrate to Internal JWT mode (V2) for gateway-based AuthN.
 * @since 0.0.1
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(name = "arda.shared.internal-jwt.enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Deprecated(since = "0.1.0", forRemoval = false)
public class LegacyKeycloakSecurityConfig {

  private final SecurityProperties properties;
  private final JwtUtils jwtUtils;
  private final MultiRealmAuthenticationManagerResolver authenticationManagerResolver;
  private final CorsConfigurationSource corsConfigurationSource;

  @Bean
  @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(SecurityFilterChain.class)
  public SecurityFilterChain legacyKeycloakFilterChain(HttpSecurity http) throws Exception {
    log.info("Configuring Security Filter Chain with Legacy Keycloak Multi-Realm mode");

    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**", "/health", "/error",
                "/api/v1/public/**", "/v1/public/**", "/v1/internal/**")
            .permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .authenticationManagerResolver(authenticationManagerResolver))
        .addFilterAfter(tenantJwtValidationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public TenantJwtValidationFilter tenantJwtValidationFilter() {
    return new TenantJwtValidationFilter(jwtUtils);
  }

  /**
   * Legacy JwtDecoder bean for backward compatibility.
   */
  @Deprecated(since = "0.0.2")
  @Bean
  public JwtDecoder jwtDecoder() {
    String jwkSetUri = properties.getJwt().getJwkSetUri();
    if (jwkSetUri == null || jwkSetUri.isBlank()) {
      String issuerUri = properties.getJwt().getIssuerUri();
      if (issuerUri != null && !issuerUri.isBlank()) {
        jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
      } else {
        jwkSetUri = properties.getJwt().getBaseKeycloakUrl() +
            "/realms/arda/protocol/openid-connect/certs";
      }
    }
    log.info("Configuring legacy JwtDecoder with JWK Set URI: {}", jwkSetUri);
    return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
  }
}
