package vn.io.arda.shared.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.io.arda.shared.multitenant.context.TenantContext;
import vn.io.arda.shared.security.properties.InternalJwtProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Security filter that verifies Internal JWTs signed by APISIX Gateway.
 * <p>
 * Replaces the heavy Keycloak multi-realm resolver when Gateway AuthN is
 * enabled.
 * This filter reads the {@code X-Internal-Token} header, verifies the RS256
 * signature
 * using the gateway's public key, extracts user/tenant claims, and sets both
 * SecurityContext and TenantContext.
 * </p>
 *
 * <p>
 * <strong>Security chain (V2):</strong>
 * </p>
 * <ol>
 * <li>APISIX Gateway verifies Keycloak JWT (AuthN)</li>
 * <li>APISIX signs Internal JWT with RS256 private key</li>
 * <li><strong>THIS FILTER</strong> verifies Internal JWT with public key (AuthZ
 * context)</li>
 * <li>TenantRoutingDataSource routes queries to tenant database</li>
 * </ol>
 *
 * @since 0.1.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 5) // Before TenantJwtValidationFilter
public class InternalJwtFilter implements Filter {

  private final InternalJwtProperties properties;
  private final ResourceLoader resourceLoader;
  private PublicKey publicKey;

  public InternalJwtFilter(InternalJwtProperties properties, ResourceLoader resourceLoader) {
    this.properties = properties;
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void init() {
    try {
      loadPublicKey();
      log.info("InternalJwtFilter initialized — Gateway AuthN mode enabled (issuer={})",
          properties.getIssuer());
    } catch (Exception e) {
      log.error("CRITICAL: Failed to load Internal JWT public key from '{}'. " +
          "All authenticated requests will fail!", properties.getPublicKeyLocation(), e);
      throw new IllegalStateException("Cannot start without Internal JWT public key", e);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Skip public endpoints
    if (isPublicEndpoint(httpRequest)) {
      chain.doFilter(request, response);
      return;
    }

    String token = httpRequest.getHeader(properties.getHeaderName());

    if (token == null || token.isBlank()) {
      log.debug("No Internal JWT found in header '{}' for URI: {}",
          properties.getHeaderName(), httpRequest.getRequestURI());
      httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      httpResponse.setContentType("application/json");
      httpResponse.getWriter().write(
          "{\"error\":\"Unauthorized\",\"message\":\"Missing Internal JWT token\"}");
      return;
    }

    try {
      // Verify and parse Internal JWT
      Claims claims = Jwts.parser()
          .verifyWith(publicKey)
          .requireIssuer(properties.getIssuer())
          .clockSkewSeconds(properties.getClockSkewSeconds())
          .build()
          .parseSignedClaims(token)
          .getPayload();

      // Extract user context
      String userId = claims.getSubject();
      String tenantId = claims.get("tid", String.class);
      String username = claims.get("username", String.class);
      String email = claims.get("email", String.class);

      // Extract roles
      @SuppressWarnings("unchecked")
      List<String> roles = claims.get("roles", List.class);
      if (roles == null) {
        roles = Collections.emptyList();
      }

      // Set SecurityContext
      List<SimpleGrantedAuthority> authorities = roles.stream()
          .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
          .collect(Collectors.toList());

      Map<String, Object> principal = new HashMap<>();
      principal.put("sub", userId);
      principal.put("tid", tenantId);
      principal.put("username", username);
      principal.put("email", email);
      principal.put("roles", roles);

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null,
          authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // Set TenantContext
      if (tenantId != null && !tenantId.isBlank()) {
        TenantContext.setTenantId(tenantId);
      }

      log.trace("Internal JWT verified — user={}, tenant={}, roles={}",
          userId, tenantId, roles);

      chain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      log.warn("Internal JWT expired for URI: {}", httpRequest.getRequestURI());
      sendError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
          "Internal JWT token has expired");
    } catch (SignatureException e) {
      log.warn("Internal JWT signature verification failed for URI: {}",
          httpRequest.getRequestURI());
      sendError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
          "Invalid Internal JWT signature");
    } catch (MalformedJwtException e) {
      log.warn("Malformed Internal JWT for URI: {}", httpRequest.getRequestURI());
      sendError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
          "Malformed Internal JWT token");
    } catch (Exception e) {
      log.error("Unexpected error verifying Internal JWT", e);
      sendError(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal authentication error");
    }
    // NOTE: Do NOT clear TenantContext here — TenantContextFilter handles cleanup
    // in its own try-finally block to ensure context is available during DB
    // routing.
  }

  private void loadPublicKey() throws Exception {
    String keyLocation = properties.getPublicKeyLocation();

    var resource = resourceLoader.getResource(keyLocation);
    String keyContent;
    try (InputStream is = resource.getInputStream()) {
      keyContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    // Strip PEM headers/footers and whitespace
    keyContent = keyContent
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s+", "");

    byte[] keyBytes = Base64.getDecoder().decode(keyContent);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    this.publicKey = keyFactory.generatePublic(spec);

    log.info("Loaded Internal JWT public key from: {}", keyLocation);
  }

  private boolean isPublicEndpoint(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/actuator/") ||
        uri.startsWith("/health") ||
        uri.startsWith("/error") ||
        uri.startsWith("/api/v1/public/") ||
        uri.startsWith("/v1/public/") ||
        uri.startsWith("/v1/internal/");
  }

  private void sendError(HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().write(
        String.format("{\"error\":\"%s\",\"message\":\"%s\"}",
            status == 401 ? "Unauthorized" : "Internal Server Error", message));
  }

  @Override
  public void destroy() {
    log.info("InternalJwtFilter destroyed");
  }
}
