
package co.edu.uco.ucochallenge.infrastructure.secondary.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf) {
        var serializer = new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer();

        var defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // TTLs por caché (opcional pero recomendado)
        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put("users.byId", defaults.entryTtl(Duration.ofMinutes(10)));
        perCache.put("users.pages", defaults.entryTtl(Duration.ofMinutes(3)));
        perCache.put("users.search", defaults.entryTtl(Duration.ofMinutes(3)));
        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build();
    }



    @Bean
    public CacheErrorHandler redisCacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                log("GET", cache, key, ex);
            }
            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                log("PUT", cache, key, ex);
            }
            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                log("EVICT", cache, key, ex);
            }
            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                log("CLEAR", cache, null, ex);
            }
            private void log(String op, Cache cache, Object key, Exception ex) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Cache operation {} failed for cache {} and key {}",
                            op, cache != null ? cache.getName() : "unknown", key, ex);
                }
            }
        };
    }
}
