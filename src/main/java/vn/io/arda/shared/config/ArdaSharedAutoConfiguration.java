package vn.io.arda.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import vn.io.arda.shared.exception.handler.GlobalExceptionHandler;
import vn.io.arda.shared.multitenant.properties.MultiTenancyProperties;
import vn.io.arda.shared.security.properties.SecurityProperties;

/**
 * Main auto-configuration class for Arda Shared Kernel.
 * Automatically configures common components when library is on classpath.
 *
 * @since 0.0.1
 */
@Slf4j
@AutoConfiguration
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
        log.info("=".repeat(80));
    }

    @Bean
    @ConditionalOnProperty(name = "arda.shared.exception-handler.enabled", havingValue = "true", matchIfMissing = true)
    public GlobalExceptionHandler globalExceptionHandler() {
        log.info("Registering GlobalExceptionHandler");
        return new GlobalExceptionHandler();
    }
}
