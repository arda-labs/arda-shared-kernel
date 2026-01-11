package vn.io.arda.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import vn.io.arda.shared.cache.TenantAwareCacheManager;
import vn.io.arda.shared.cache.properties.CachingProperties;

import java.time.Duration;

/**
 * Auto-configuration for caching features with Redis.
 *
 * @since 0.0.1
 */
@Slf4j
@AutoConfiguration
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(name = "arda.shared.caching.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CachingProperties.class)
@RequiredArgsConstructor
public class CachingAutoConfiguration {

    private final CachingProperties properties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Configuring Redis connection: {}:{}",
                properties.getRedis().getHost(),
                properties.getRedis().getPort());

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(properties.getRedis().getHost());
        config.setPort(properties.getRedis().getPort());

        if (properties.getRedis().getPassword() != null) {
            config.setPassword(properties.getRedis().getPassword());
        }

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Configuring RedisCacheManager");

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .transactionAware()
                .build();
    }

    @Bean
    public TenantAwareCacheManager tenantAwareCacheManager(RedisCacheManager cacheManager) {
        log.info("Registering TenantAwareCacheManager");
        return new TenantAwareCacheManager(cacheManager);
    }
}
