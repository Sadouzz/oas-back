package sn.oas.facturation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Map;

//@Configuration
public class RedisConfig {

    //@Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));

        Map<String, RedisCacheConfiguration> perCache = Map.of(
                "produits", config.entryTtl(Duration.ofHours(1)),
                "utilisateurs", config.entryTtl(Duration.ofMinutes(5)),
                "dashboard_agent", config.entryTtl(Duration.ofMinutes(5)),
                "dashboard_chef_atelier", config.entryTtl(Duration.ofMinutes(5)),
                "dashboard_agent_magasin", config.entryTtl(Duration.ofMinutes(5)),
                "dashboard_super_agent", config.entryTtl(Duration.ofMinutes(10)),
                "clients_page", config.entryTtl(Duration.ofMinutes(10)),
                "piece_stats", config.entryTtl(Duration.ofMinutes(5))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(perCache)
                .build();
    }
}

