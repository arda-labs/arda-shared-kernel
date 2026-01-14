package vn.io.arda.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.io.arda.shared.security.filter.TenantJwtValidationFilter;
import vn.io.arda.shared.security.jwt.JwtUtils;
import vn.io.arda.shared.security.jwt.MultiRealmAuthenticationManagerResolver;
import vn.io.arda.shared.security.properties.SecurityProperties;

/**
 * Auto-configuration for security features.
 * Configures JWT authentication with Keycloak.
 *
 * @since 0.0.1
 */
@Slf4j
@AutoConfiguration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@ConditionalOnProperty(name = "arda.shared.security.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class SecurityAutoConfiguration {

    private final SecurityProperties properties;
    private final MultiRealmAuthenticationManagerResolver authenticationManagerResolver;
    private final JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain with Multi-Realm JWT authentication");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/health", "/error", "/api/v1/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationManagerResolver(authenticationManagerResolver)
                )
                .addFilterAfter(tenantJwtValidationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates TenantJwtValidationFilter bean.
     * This filter validates that JWT tenant_id matches X-Tenant-ID header.
     *
     * @return TenantJwtValidationFilter instance
     */
    @Bean
    public TenantJwtValidationFilter tenantJwtValidationFilter() {
        return new TenantJwtValidationFilter(jwtUtils);
    }

    /**
     * Legacy JwtDecoder bean for backward compatibility.
     * Services using multi-realm should use MultiRealmAuthenticationManagerResolver instead.
     *
     * @return JwtDecoder for default realm
     * @deprecated Use MultiRealmAuthenticationManagerResolver for multi-realm support
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
                // Fallback to base URL + default realm
                jwkSetUri = properties.getJwt().getBaseKeycloakUrl() +
                        "/realms/arda/protocol/openid-connect/certs";
            }
        }

        log.info("Configuring legacy JwtDecoder with JWK Set URI: {}", jwkSetUri);
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS with allowed origins: {}",
                properties.getCors().getAllowedOrigins());

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(properties.getCors().getAllowedMethods());
        configuration.setAllowedHeaders(properties.getCors().getAllowedHeaders());
        configuration.setMaxAge(properties.getCors().getMaxAge());
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
