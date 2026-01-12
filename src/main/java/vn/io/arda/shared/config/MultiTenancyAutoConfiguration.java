package vn.io.arda.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.io.arda.shared.multitenant.datasource.TenantDataSourceCache;
import vn.io.arda.shared.multitenant.datasource.TenantRoutingDataSource;
import vn.io.arda.shared.multitenant.filter.TenantContextFilter;
import vn.io.arda.shared.multitenant.interceptor.TenantInterceptor;
import vn.io.arda.shared.multitenant.properties.MultiTenancyProperties;
import vn.io.arda.shared.multitenant.resolver.CompositeTenantResolver;
import vn.io.arda.shared.multitenant.resolver.HeaderTenantResolver;
import vn.io.arda.shared.multitenant.resolver.JwtTenantResolver;
import vn.io.arda.shared.multitenant.resolver.TenantResolver;
import vn.io.arda.shared.multitenant.service.CentralPlatformTenantService;
import vn.io.arda.shared.multitenant.service.TenantMetadataService;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * Auto-configuration for multi-tenancy features with thread-safe tenant context management.
 *
 * <p><strong>Key Components:</strong></p>
 * <ul>
 *   <li>{@link TenantContextFilter} - Servlet filter with try-finally for guaranteed cleanup (HIGHEST_PRECEDENCE)</li>
 *   <li>{@link TenantInterceptor} - Spring MVC interceptor for logging and monitoring</li>
 *   <li>{@link TenantRoutingDataSource} - Dynamic database routing per tenant</li>
 *   <li>{@link TenantDataSourceCache} - Caffeine-based DataSource caching with LRU eviction</li>
 * </ul>
 *
 * <p><strong>Thread-Safety:</strong></p>
 * <p>The {@code TenantContextFilter} uses try-finally to ensure {@link vn.io.arda.shared.multitenant.context.TenantContext#clear()}
 * is ALWAYS called, even when exceptions occur. This prevents tenant context leakage in servlet container thread pools.</p>
 *
 * @since 0.0.1
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(name = "arda.shared.multi-tenancy.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MultiTenancyProperties.class)
@RequiredArgsConstructor
public class MultiTenancyAutoConfiguration implements WebMvcConfigurer {

    private final MultiTenancyProperties properties;

    @Bean
    public TenantResolver tenantResolver() {
        log.info("Configuring TenantResolver with header: {}", properties.getTenantHeader());

        return new CompositeTenantResolver(Arrays.asList(
                new HeaderTenantResolver(properties.getTenantHeader()),
                new JwtTenantResolver()
        ));
    }

    /**
     * Registers TenantContextFilter with HIGHEST_PRECEDENCE for thread-safe tenant context management.
     * This filter ensures tenant context is ALWAYS cleared, even on exceptions.
     */
    @Bean
    public FilterRegistrationBean<TenantContextFilter> tenantContextFilter(TenantResolver tenantResolver) {
        FilterRegistrationBean<TenantContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantContextFilter(tenantResolver));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE); // Run before any other filter
        registration.setName("tenantContextFilter");
        log.info("TenantContextFilter registered with HIGHEST_PRECEDENCE for thread-safe context management");
        return registration;
    }

    @Bean
    public TenantInterceptor tenantInterceptor(TenantResolver tenantResolver) {
        log.info("Registering TenantInterceptor for request logging");
        return new TenantInterceptor(tenantResolver);
    }

    @Bean
    public TenantMetadataService tenantMetadataService() {
        log.info("Configuring TenantMetadataService with central platform: {}",
                properties.getCentralPlatformUrl());
        return new CentralPlatformTenantService(properties);
    }

    @Bean
    public TenantDataSourceCache tenantDataSourceCache() {
        log.info("Initializing TenantDataSourceCache");
        return new TenantDataSourceCache(properties);
    }

    @Bean
    @Primary
    public DataSource dataSource(TenantDataSourceCache dataSourceCache,
                                 TenantMetadataService tenantMetadataService) {
        log.info("Configuring TenantRoutingDataSource");
        return new TenantRoutingDataSource(dataSourceCache, tenantMetadataService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor(tenantResolver()))
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/error", "/health");

        log.info("TenantInterceptor registered for all paths except actuator endpoints");
    }
}
