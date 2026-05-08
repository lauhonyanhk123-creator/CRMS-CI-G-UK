package com.crms.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        // shorter TTL for frequently-changing data
        cacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("contracts", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        // remaining cache names from original ConcurrentMapCacheManager
        cacheConfigs.put("roles", defaultConfig);
        cacheConfigs.put("operatives", defaultConfig);
        cacheConfigs.put("subcontractors", defaultConfig);
        cacheConfigs.put("applications", defaultConfig);
        cacheConfigs.put("certificates", defaultConfig);

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
