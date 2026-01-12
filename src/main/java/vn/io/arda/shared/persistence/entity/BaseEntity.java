package vn.io.arda.shared.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base entity class for all domain entities.
 * Provides common fields: id, createdAt, updatedAt, version for optimistic locking, and soft delete flag.
 *
 * <p><strong>Spring Data JPA Auditing:</strong></p>
 * <ul>
 *   <li>{@code @CreatedDate} and {@code @LastModifiedDate} require {@code @EntityListeners(AuditingEntityListener.class)}</li>
 *   <li>Enable auditing in your configuration with {@code @EnableJpaAuditing}</li>
 *   <li>Timestamps are automatically managed by Spring Data JPA</li>
 * </ul>
 *
 * @since 0.0.1
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
