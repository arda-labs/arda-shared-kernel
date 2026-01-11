package vn.io.arda.shared.security.annotation;

import java.lang.annotation.*;

/**
 * Method-level annotation for tenant access control.
 * Validates that the current user has access to the specified tenant.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @TenantAccess
 * public void processOrder(String tenantId, Order order) {
 *     // Method body
 * }
 * }</pre>
 *
 * @since 0.0.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantAccess {

    /**
     * Parameter name containing the tenant ID.
     * Default is "tenantId".
     */
    String value() default "tenantId";
}
