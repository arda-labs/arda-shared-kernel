package vn.io.arda.shared.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;

/**
 * Soft-deletable entity class that extends AuditableEntity.
 * Provides soft delete functionality instead of physical deletion.
 *
 * @since 0.0.1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditableEntity {

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    /**
     * Performs a soft delete on this entity.
     * Sets deleted flag to true and records deletion timestamp and user.
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.deletedBy = getCurrentUsername();
    }

    /**
     * Restores a soft-deleted entity.
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * Checks if this entity is deleted.
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
