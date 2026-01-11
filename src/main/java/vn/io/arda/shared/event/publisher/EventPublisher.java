package vn.io.arda.shared.event.publisher;

import vn.io.arda.shared.event.DomainEvent;

/**
 * Interface for publishing domain events.
 *
 * @since 0.0.1
 */
public interface EventPublisher {

    /**
     * Publishes a domain event.
     *
     * @param event the event to publish
     * @param <T> the event type
     */
    <T extends DomainEvent> void publish(T event);
}
