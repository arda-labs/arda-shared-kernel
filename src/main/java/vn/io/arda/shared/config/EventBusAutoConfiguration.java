package vn.io.arda.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import vn.io.arda.shared.event.properties.EventBusProperties;
import vn.io.arda.shared.event.publisher.EventPublisher;
import vn.io.arda.shared.event.publisher.SpringEventPublisher;

/**
 * Auto-configuration for event bus features.
 *
 * @since 0.0.1
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(name = "arda.shared.event-bus.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(EventBusProperties.class)
@RequiredArgsConstructor
public class EventBusAutoConfiguration {

    private final EventBusProperties properties;

    @Bean
    @ConditionalOnProperty(name = "arda.shared.event-bus.type", havingValue = "spring", matchIfMissing = true)
    public EventPublisher springEventPublisher(org.springframework.context.ApplicationEventPublisher publisher) {
        log.info("Configuring Spring Event Publisher");
        return new SpringEventPublisher(publisher);
    }

    // Kafka event publisher would be added here if type=kafka
    // @Bean
    // @ConditionalOnProperty(name = "arda.shared.event-bus.type", havingValue = "kafka")
    // public EventPublisher kafkaEventPublisher() { ... }
}
