package vn.io.arda.shared.persistence.listener;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.io.arda.shared.persistence.entity.AuditableEntity;

/**
 * JPA EntityListener that automatically populates audit fields (createdBy, updatedBy)
 * based on the current authenticated user from Spring Security context.
 * <p>
 * This listener is automatically applied to entities that extend {@link AuditableEntity}.
 * It extracts the username from the SecurityContext and sets it in the audit fields
 * during entity persistence and update operations.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>
 * &#64;Entity
 * &#64;EntityListeners(TenantAwareAuditingListener.class)
 * public class User extends AuditableEntity&lt;Long&gt; {
 *     // Entity fields
 * }
 * </pre>
 *
 * <p>The listener will automatically set:</p>
 * <ul>
 *   <li>createdBy - set once during entity creation</li>
 *   <li>updatedBy - updated on every entity modification</li>
 * </ul>
 *
 * @author Arda Development Team
 * @see AuditableEntity
 */
@Slf4j
@Component
public class TenantAwareAuditingListener {

    private static final String SYSTEM_USER = "system";

    /**
     * Called before entity persist operation.
     * Sets createdBy field with the current authenticated user.
     *
     * @param entity the entity being persisted
     */
    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            String currentUser = getCurrentUsername();
            log.debug("Setting createdBy for entity {} to user: {}", entity.getClass().getSimpleName(), currentUser);

            auditable.setCreatedBy(currentUser);
            auditable.setUpdatedBy(currentUser);
        }
    }

    /**
     * Called before entity update operation.
     * Updates the updatedBy field with the current authenticated user.
     *
     * @param entity the entity being updated
     */
    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            String currentUser = getCurrentUsername();
            log.debug("Setting updatedBy for entity {} to user: {}", entity.getClass().getSimpleName(), currentUser);

            auditable.setUpdatedBy(currentUser);
        }
    }

    /**
     * Retrieves the current authenticated username from Spring Security context.
     * Falls back to "system" if no authentication is present.
     *
     * @return the current username or "system"
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();

                // Exclude anonymous user
                if (!"anonymousUser".equals(username)) {
                    return username;
                }
            }
        } catch (Exception e) {
            log.trace("Unable to retrieve authentication from SecurityContext", e);
        }

        return SYSTEM_USER;
    }
}
