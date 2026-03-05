package vn.io.arda.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import vn.io.arda.shared.security.filter.InternalJwtFilter;
import vn.io.arda.shared.security.properties.InternalJwtProperties;

/**
 * Security configuration for Internal JWT mode (V2 — Gateway AuthN).
 * <p>
 * Activated when {@code arda.shared.internal-jwt.enabled=true}.
 * APISIX Gateway handles Keycloak AuthN, signs Internal JWT (RS256),
 * and this config verifies it with the public key.
 * </p>
 *
 * @since 0.1.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(name = "arda.shared.internal-jwt.enabled", havingValue = "true")
@EnableConfigurationProperties(InternalJwtProperties.class)
@RequiredArgsConstructor
public class InternalJwtSecurityConfig {

  private final InternalJwtProperties internalJwtProperties;
  private final ResourceLoader resourceLoader;
  private final CorsConfigurationSource corsConfigurationSource;

  @Bean
  @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(SecurityFilterChain.class)
  public SecurityFilterChain internalJwtFilterChain(HttpSecurity http) throws Exception {
    log.info("Configuring Security Filter Chain with Internal JWT mode (Gateway AuthN)");

    InternalJwtFilter internalJwtFilter = new InternalJwtFilter(internalJwtProperties, resourceLoader);
    internalJwtFilter.init();

    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**", "/health", "/error",
                "/api/v1/public/**", "/v1/public/**", "/v1/internal/**")
            .permitAll()
            .anyRequest().authenticated())
        // Disable OAuth2 resource server — Gateway handles AuthN
        .oauth2ResourceServer(AbstractHttpConfigurer::disable)
        // Add Internal JWT filter before Spring Security's auth processing
        .addFilterBefore(internalJwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
