package vn.io.arda.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import vn.io.arda.shared.exception.handler.GlobalExceptionHandler;
import vn.io.arda.shared.multitenant.properties.MultiTenancyProperties;
import vn.io.arda.shared.security.properties.SecurityProperties;

/**
 * Main auto-configuration class for Arda Shared Kernel.
 * Automatically configures common components when library is on classpath.
 *
 * <p><strong>Features enabled:</strong></p>
 * <ul>
 *   <li>JPA Auditing with {@code @EnableJpaAuditing} for automatic timestamp and user tracking</li>
 *   <li>Component scanning for all shared beans</li>
 *   <li>Exception handling with {@link GlobalExceptionHandler}</li>
 *   <li>Security utilities and JWT processing</li>
 * </ul>
 *
 * <p><strong>JPA Auditing Configuration:</strong></p>
 * <p>JPA Auditing is enabled with {@code auditorAwareRef = "securityAuditorAware"}, which uses
 * {@link vn.io.arda.shared.persistence.auditing.SecurityAuditorAware} to automatically populate:</p>
 * <ul>
 *   <li>{@code createdBy} and {@code updatedBy} fields in {@link vn.io.arda.shared.persistence.entity.AuditableEntity}</li>
 *   <li>{@code createdAt} and {@code updatedAt} fields in {@link vn.io.arda.shared.persistence.entity.BaseEntity}</li>
 * </ul>
 *
 * @see vn.io.arda.shared.persistence.auditing.SecurityAuditorAware
 * @see vn.io.arda.shared.persistence.entity.BaseEntity
 * @see vn.io.arda.shared.persistence.entity.AuditableEntity
 * @since 0.0.1
 */
@Slf4j
@AutoConfiguration
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
@ComponentScan(basePackages = "vn.io.arda.shared")
@EnableConfigurationProperties({
        MultiTenancyProperties.class,
        SecurityProperties.class
})
public class ArdaSharedAutoConfiguration {

    public ArdaSharedAutoConfiguration() {
        log.info("=".repeat(80));
        log.info("Arda Shared Kernel Auto-Configuration Initialized");
        log.info("Version: 0.0.1-SNAPSHOT");
        log.info("JPA Auditing: ENABLED (auditorAwareRef=securityAuditorAware)");
        log.info("=".repeat(80));
    }

    @Bean
    @ConditionalOnProperty(name = "arda.shared.exception-handler.enabled", havingValue = "true", matchIfMissing = true)
    public GlobalExceptionHandler globalExceptionHandler() {
        log.info("Registering GlobalExceptionHandler");
        return new GlobalExceptionHandler();
    }
}
