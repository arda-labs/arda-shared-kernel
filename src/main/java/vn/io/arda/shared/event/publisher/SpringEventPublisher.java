package vn.io.arda.shared.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import vn.io.arda.shared.event.DomainEvent;

/**
 * Spring-based event publisher using ApplicationEventPublisher.
 * Configured as a bean in {@link vn.io.arda.shared.config.EventBusAutoConfiguration}.
 *
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Async
    public <T extends DomainEvent> void publish(T event) {
        log.debug("Publishing event: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
