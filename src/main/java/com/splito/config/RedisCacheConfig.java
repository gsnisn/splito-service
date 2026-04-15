package com.splito.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     *  REDIS CACHE MANAGER (used when redis.enabled = true)
     *
     * ==> What this bean does:
     * - Connects to Redis
     * - Stores cache in Redis server
     * - Shared across all instances (distributed cache)
     */
    @Bean
    @ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        GenericJacksonJsonRedisSerializer serializer =
                GenericJacksonJsonRedisSerializer.builder()
                        .enableDefaultTyping(
                                BasicPolymorphicTypeValidator.builder()
                                        .allowIfSubType(Object.class)
                                        .build()
                        )
                        .build();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                )
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("groups", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("groupBalances", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("userList", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("groupList", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    /**
     *  FALLBACK CACHE MANAGER (used when redis.enabled = false OR missing)
     *
     * ==> What this bean does:
     * - Stores cache in JVM memory (HashMap-like)
     * - No external dependency (no Redis needed)
     * - Fast but NOT shared across instances
     */
    @Bean
    @ConditionalOnProperty(
            name = "redis.enabled",
            havingValue = "false",
            matchIfMissing = true // If property not present, fallback is used
    )
    public CacheManager simpleCacheManager() {

        // In-memory cache (per application instance)
        return new ConcurrentMapCacheManager(
                "users", "groups", "groupBalances", "userList", "groupList"
        );
    }
}