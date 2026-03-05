package vn.io.arda.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.io.arda.shared.multitenant.cache.TenantConfigLocalCache;
import vn.io.arda.shared.multitenant.datasource.TenantDataSourceCache;
import vn.io.arda.shared.multitenant.datasource.TenantRoutingDataSource;
import vn.io.arda.shared.multitenant.filter.TenantContextFilter;
import vn.io.arda.shared.multitenant.interceptor.TenantInterceptor;
import vn.io.arda.shared.multitenant.interceptor.TenantMaintenanceInterceptor;
import vn.io.arda.shared.multitenant.properties.MultiTenancyProperties;
import vn.io.arda.shared.multitenant.resolver.CompositeTenantResolver;
import vn.io.arda.shared.multitenant.resolver.HeaderTenantResolver;
import vn.io.arda.shared.multitenant.resolver.JwtTenantResolver;
import vn.io.arda.shared.multitenant.resolver.TenantResolver;
import vn.io.arda.shared.multitenant.service.TenantMetadataService;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * Auto-configuration for multi-tenancy features.
 * <p>
 * Supports two modes for tenant config resolution:
 * <ul>
 * <li><strong>REST mode</strong> (default): Uses
 * {@code CentralPlatformTenantService}
 * to fetch config via REST API on each request.</li>
 * <li><strong>Event-driven mode</strong>: Uses
 * {@code EventDrivenTenantMetadataService}
 * with Kafka events + local cache for zero-latency lookups.</li>
 * </ul>
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

    @Autowired(required = false)
    private TenantMaintenanceInterceptor maintenanceInterceptor;

    @Bean
    public TenantResolver tenantResolver() {
        log.info("Configuring TenantResolver with header: {}", properties.getTenantHeader());
        return new CompositeTenantResolver(Arrays.asList(
                new HeaderTenantResolver(properties.getTenantHeader()),
                new JwtTenantResolver()));
    }

    @Bean
    public FilterRegistrationBean<TenantContextFilter> tenantContextFilter(TenantResolver tenantResolver) {
        FilterRegistrationBean<TenantContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantContextFilter(tenantResolver));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("tenantContextFilter");
        log.info("TenantContextFilter registered with HIGHEST_PRECEDENCE");
        return registration;
    }

    @Bean
    public TenantInterceptor tenantInterceptor(TenantResolver tenantResolver) {
        return new TenantInterceptor(tenantResolver);
    }

    @Bean
    public TenantDataSourceCache tenantDataSourceCache() {
        return new TenantDataSourceCache(properties);
    }

    @Bean
    @Primary
    public DataSource dataSource(TenantDataSourceCache dataSourceCache,
            TenantMetadataService tenantMetadataService) {
        log.info("Configuring TenantRoutingDataSource");
        return new TenantRoutingDataSource(dataSourceCache, tenantMetadataService);
    }

    // --- V2: Event-driven tenant config beans ---

    @Bean
    @ConditionalOnProperty(name = "arda.shared.tenant-config.mode", havingValue = "event-driven")
    public TenantConfigLocalCache tenantConfigLocalCache() {
        int maxSize = properties.getDatasourceCache().getMaxSize();
        log.info("Initializing TenantConfigLocalCache (event-driven mode, maxSize={})", maxSize);
        return new TenantConfigLocalCache(maxSize);
    }

    @Bean
    @ConditionalOnProperty(name = "arda.shared.tenant-config.mode", havingValue = "event-driven")
    public TenantMaintenanceInterceptor tenantMaintenanceInterceptor(TenantConfigLocalCache configLocalCache) {
        log.info("Registering TenantMaintenanceInterceptor for per-tenant 503 guard");
        return new TenantMaintenanceInterceptor(configLocalCache);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor(tenantResolver()))
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/error", "/health");

        // Register maintenance interceptor only when available (event-driven mode)
        if (maintenanceInterceptor != null) {
            registry.addInterceptor(maintenanceInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/actuator/**", "/error", "/health");
            log.info("TenantMaintenanceInterceptor registered for per-tenant maintenance guard");
        }
    }
}
