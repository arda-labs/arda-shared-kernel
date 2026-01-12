package vn.io.arda.shared.persistence.auditing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Spring Data JPA AuditorAware implementation that extracts the current authenticated user
 * from Spring Security's SecurityContext.
 *
 * <p>This component is used by Spring Data JPA auditing to automatically populate
 * {@code @CreatedBy} and {@code @LastModifiedBy} fields in entities extending
 * {@link vn.io.arda.shared.persistence.entity.AuditableEntity}.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * // Enable JPA Auditing in your configuration
 * {@literal @}EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
 * {@literal @}Configuration
 * public class JpaConfig {
 *     // Configuration
 * }
 * </pre>
 *
 * <p><strong>Auditor Resolution Strategy:</strong></p>
 * <ol>
 *   <li>Extract {@link Authentication} from {@link SecurityContextHolder}</li>
 *   <li>Check if authentication is present and authenticated</li>
 *   <li>Exclude "anonymousUser" (Spring Security default for unauthenticated requests)</li>
 *   <li>Return username or fallback to "system"</li>
 * </ol>
 *
 * <p><strong>Fallback Scenarios:</strong></p>
 * <ul>
 *   <li>No authentication → "system"</li>
 *   <li>Anonymous user → "system"</li>
 *   <li>Exception during authentication retrieval → "system"</li>
 *   <li>Background jobs / scheduled tasks → "system"</li>
 * </ul>
 *
 * @see org.springframework.data.domain.AuditorAware
 * @see org.springframework.data.annotation.CreatedBy
 * @see org.springframework.data.annotation.LastModifiedBy
 * @see vn.io.arda.shared.persistence.entity.AuditableEntity
 * @since 0.0.1
 */
@Slf4j
@Component("securityAuditorAware")
public class SecurityAuditorAware implements AuditorAware<String> {

    /**
     * Default auditor for system operations (background jobs, migrations, etc.)
     */
    private static final String SYSTEM_USER = "system";

    /**
     * Spring Security's default name for unauthenticated users
     */
    private static final String ANONYMOUS_USER = "anonymousUser";

    /**
     * Gets the current auditor (username) from Spring Security context.
     * Always returns a non-null Optional, either with the authenticated username or "system".
     *
     * @return Optional containing the current username, or "system" if not authenticated (never null or empty)
     */
    @Override
    @org.springframework.lang.NonNull
    public Optional<String> getCurrentAuditor() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();

                // Exclude anonymous user - treat as system operation
                if (!ANONYMOUS_USER.equals(username)) {
                    log.trace("Current auditor resolved: {}", username);
                    return Optional.of(username);
                }
            }
        } catch (Exception e) {
            // Log at trace level - this is expected for non-authenticated contexts
            log.trace("Unable to retrieve authentication from SecurityContext: {}", e.getMessage());
        }

        log.trace("No authenticated user found, using system auditor");
        return Optional.of(SYSTEM_USER);
    }
}
