package vn.io.arda.shared.event;

import java.time.Instant;

/**
 * Base interface for all domain events.
 *
 * @since 0.0.1
 */
public interface DomainEvent {

    /**
     * Gets the timestamp when this event occurred.
     */
    Instant getOccurredAt();
}
