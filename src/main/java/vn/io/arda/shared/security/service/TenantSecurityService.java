package vn.io.arda.shared.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import vn.io.arda.shared.exception.UnauthorizedException;
import vn.io.arda.shared.multitenant.context.TenantContext;

import java.util.List;
import java.util.Map;

/**
 * Service for validating tenant access and permissions based on JWT claims and SecurityContext.
 * <p>
 * This service ensures that the authenticated user has access to the requested tenant
 * by validating JWT claims (tenant_id, tenants) against the current tenant context.
 * It prevents cross-tenant data access and enforces tenant isolation at the application level.
 * </p>
 *
 * <p>Usage example in controllers:</p>
 * <pre>
 * &#64;RestController
 * &#64;RequestMapping("/api/users")
 * public class UserController {
 *     &#64;Autowired
 *     private TenantSecurityService tenantSecurityService;
 *
 *     &#64;GetMapping
 *     public List&lt;User&gt; getUsers() {
 *         tenantSecurityService.validateTenantAccess();
 *         // ... fetch users
 *     }
 * }
 * </pre>
 *
 * @author Arda Development Team
 * @see TenantContext
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantSecurityService {

    private static final String TENANT_ID_CLAIM = "tenant_id";
    private static final String TENANTS_CLAIM = "tenants";

    /**
     * Validates that the current authenticated user has access to the current tenant.
     * Checks JWT claims for tenant_id or tenants list.
     *
     * @throws UnauthorizedException if user does not have access to the current tenant
     */
    public void validateTenantAccess() {
        String currentTenant = TenantContext.getCurrentTenant();

        if (currentTenant == null || currentTenant.isBlank()) {
            log.warn("No tenant context found during access validation");
            throw new UnauthorizedException("Tenant context is required");
        }

        validateTenantAccess(currentTenant);
    }

    /**
     * Validates that the current authenticated user has access to the specified tenant.
     *
     * @param tenantId the tenant ID to validate access for
     * @throws UnauthorizedException if user does not have access to the tenant
     */
    public void validateTenantAccess(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new UnauthorizedException("Tenant ID is required");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found during tenant access validation");
            throw new UnauthorizedException("Authentication is required");
        }

        // Check if principal is a JWT
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            validateTenantFromJwt(jwt, tenantId);
        } else {
            log.warn("Authentication principal is not a JWT, skipping tenant validation");
        }
    }

    /**
     * Validates tenant access from JWT claims.
     *
     * @param jwt      the JWT token
     * @param tenantId the requested tenant ID
     * @throws UnauthorizedException if JWT does not contain valid tenant claims
     */
    private void validateTenantFromJwt(Jwt jwt, String tenantId) {
        // Check single tenant_id claim
        String jwtTenantId = jwt.getClaimAsString(TENANT_ID_CLAIM);
        if (jwtTenantId != null && jwtTenantId.equals(tenantId)) {
            log.debug("User has access to tenant {} via tenant_id claim", tenantId);
            return;
        }

        // Check tenants array claim
        Object tenantsObj = jwt.getClaim(TENANTS_CLAIM);
        if (tenantsObj instanceof List<?> tenantsList) {
            boolean hasAccess = tenantsList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .anyMatch(t -> t.equals(tenantId));

            if (hasAccess) {
                log.debug("User has access to tenant {} via tenants claim", tenantId);
                return;
            }
        }

        // No valid tenant claim found
        log.warn("User {} attempted to access tenant {} without proper claims",
                jwt.getSubject(), tenantId);
        throw new UnauthorizedException("User does not have access to tenant: " + tenantId);
    }

    /**
     * Retrieves the tenant ID from the current JWT token.
     *
     * @return the tenant ID from JWT claims, or null if not found
     */
    public String getTenantIdFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString(TENANT_ID_CLAIM);
        }

        return null;
    }

    /**
     * Retrieves all tenant IDs the current user has access to from JWT claims.
     *
     * @return list of tenant IDs, or empty list if none found
     */
    @SuppressWarnings("unchecked")
    public List<String> getAllTenantsFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object tenantsObj = jwt.getClaim(TENANTS_CLAIM);

            if (tenantsObj instanceof List<?> list) {
                return list.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }
        }

        return List.of();
    }

    /**
     * Checks if the current user has access to the specified tenant.
     *
     * @param tenantId the tenant ID to check
     * @return true if user has access, false otherwise
     */
    public boolean hasAccessToTenant(String tenantId) {
        try {
            validateTenantAccess(tenantId);
            return true;
        } catch (UnauthorizedException e) {
            return false;
        }
    }
}
