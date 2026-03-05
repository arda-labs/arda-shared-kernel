package vn.io.arda.shared.multitenant.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import vn.io.arda.shared.multitenant.cache.TenantConfigLocalCache;
import vn.io.arda.shared.multitenant.context.TenantContext;

/**
 * Interceptor that blocks requests to tenants currently in maintenance mode.
 * <p>
 * When a tenant is locked (via Kafka LOCKED event), this interceptor returns
 * 503 Service Unavailable with a JSON error message. This is granular per
 * tenant —
 * other tenants continue operating normally.
 * </p>
 *
 * @since 0.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class TenantMaintenanceInterceptor implements HandlerInterceptor {

  private final TenantConfigLocalCache configLocalCache;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    String tenantId = TenantContext.getTenantId().orElse(null);

    if (tenantId != null && configLocalCache.isInMaintenance(tenantId)) {
      log.warn("Request blocked — tenant '{}' is in maintenance mode. URI: {}",
          tenantId, request.getRequestURI());

      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      response.setContentType("application/json");
      response.getWriter().write(String.format(
          "{\"error\":\"Service Unavailable\"," +
              "\"message\":\"Tenant '%s' is currently under maintenance. Please try again later.\"," +
              "\"tenantId\":\"%s\"}",
          tenantId, tenantId));
      return false;
    }

    return true;
  }
}
