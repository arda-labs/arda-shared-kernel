package vn.io.arda.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.io.arda.shared.multitenant.datasource.TenantDataSourceCache;
import vn.io.arda.shared.multitenant.datasource.TenantRoutingDataSource;
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
 * Auto-configuration for multi-tenancy features.
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

    @Bean
    public TenantInterceptor tenantInterceptor(TenantResolver tenantResolver) {
        log.info("Registering TenantInterceptor");
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
