package vn.io.arda.shared.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import vn.io.arda.shared.persistence.entity.BaseEntity;

import java.util.List;
import java.util.Optional;

/**
 * Base repository interface that provides common CRUD operations and query methods
 * for all entities extending {@link BaseEntity}.
 * <p>
 * This interface extends Spring Data JPA's {@link JpaRepository} and {@link JpaSpecificationExecutor}
 * to provide standard CRUD operations plus dynamic query capabilities using Specifications.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>
 * public interface UserRepository extends BaseRepository&lt;User, Long&gt; {
 *     // Custom query methods
 *     Optional&lt;User&gt; findByEmail(String email);
 * }
 * </pre>
 *
 * @param <T>  the entity type extending BaseEntity
 * @param <ID> the entity ID type
 * @author Arda Development Team
 * @see BaseEntity
 * @see JpaRepository
 * @see JpaSpecificationExecutor
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID> extends
        JpaRepository<T, ID>,
        JpaSpecificationExecutor<T> {

    /**
     * Finds all non-deleted (active) entities.
     * This method filters out soft-deleted entities (where deleted = true).
     *
     * @return list of all active entities
     */
    default List<T> findAllActive() {
        return findAll((root, query, cb) ->
                cb.equal(root.get("deleted"), false)
        );
    }

    /**
     * Finds an active (non-deleted) entity by its ID.
     *
     * @param id the entity ID
     * @return Optional containing the entity if found and not deleted, empty otherwise
     */
    default Optional<T> findActiveById(ID id) {
        return findOne((root, query, cb) ->
                cb.and(
                        cb.equal(root.get("id"), id),
                        cb.equal(root.get("deleted"), false)
                )
        );
    }

    /**
     * Soft deletes an entity by setting its deleted flag to true.
     * Does not physically remove the entity from the database.
     *
     * @param entity the entity to soft delete
     * @return the soft-deleted entity
     */
    default T softDelete(T entity) {
        entity.setDeleted(true);
        return save(entity);
    }

    /**
     * Soft deletes an entity by its ID.
     *
     * @param id the entity ID
     * @return Optional containing the soft-deleted entity if found
     */
    default Optional<T> softDeleteById(ID id) {
        return findById(id).map(this::softDelete);
    }

    /**
     * Restores a soft-deleted entity by setting its deleted flag to false.
     *
     * @param entity the entity to restore
     * @return the restored entity
     */
    default T restore(T entity) {
        entity.setDeleted(false);
        return save(entity);
    }

    /**
     * Restores a soft-deleted entity by its ID.
     *
     * @param id the entity ID
     * @return Optional containing the restored entity if found
     */
    default Optional<T> restoreById(ID id) {
        return findById(id).map(this::restore);
    }

    /**
     * Checks if an active entity exists with the given ID.
     *
     * @param id the entity ID
     * @return true if an active entity exists, false otherwise
     */
    default boolean existsActiveById(ID id) {
        return findActiveById(id).isPresent();
    }

    /**
     * Counts all active (non-deleted) entities.
     *
     * @return count of active entities
     */
    default long countActive() {
        return count((root, query, cb) ->
                cb.equal(root.get("deleted"), false)
        );
    }
}
