package vn.io.arda.shared.event.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for event bus features.
 *
 * @since 0.0.1
 */
@Data
@ConfigurationProperties(prefix = "arda.shared.event-bus")
public class EventBusProperties {

    /**
     * Enable or disable event bus features.
     */
    private boolean enabled = true;

    /**
     * Event bus type: spring or kafka.
     */
    private EventBusType type = EventBusType.SPRING;

    /**
     * Kafka configuration.
     */
    private KafkaConfig kafka = new KafkaConfig();

    public enum EventBusType {
        SPRING,
        KAFKA
    }

    @Data
    public static class KafkaConfig {
        /**
         * Kafka bootstrap servers.
         */
        private String bootstrapServers = "localhost:9092";

        /**
         * Topic prefix for events.
         */
        private String topicPrefix = "arda";
    }
}
