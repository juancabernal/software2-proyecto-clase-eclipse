package co.edu.uco.ucochallenge.infrastructure.secondary.config;

import co.edu.uco.ucochallenge.application.user.getUser.dto.GetUserOutputDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf) {
        var serializer = new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer();

        var defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // TTLs por caché (opcional pero recomendado)
        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put("users.byId",  defaults.entryTtl(Duration.ofMinutes(10)));
        perCache.put("users.pages", defaults.entryTtl(Duration.ofMinutes(3)));
        perCache.put("users.search", defaults.entryTtl(Duration.ofMinutes(3)));
        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build();
    }

}