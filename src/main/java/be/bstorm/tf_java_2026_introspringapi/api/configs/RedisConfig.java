package be.bstorm.tf_java_2026_introspringapi.api.configs;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Configuration Redis pour le caching et le rate limiting.
 * Active l'annotation @Cacheable/@CacheEvict pour l'optimisation des performances.
 * Utilise StringRedisTemplate pour le rate limiting avec bucket à jetons.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Gestionnaire de cache Redis.
     * TTL par défaut: 15 minutes pour les entrées en cache.
     * @param connectionFactory usine de connexion Redis
     * @return gestionnaire RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15));

        return RedisCacheManager.create(connectionFactory);
    }

    /**
     * Template Redis générique pour les objets.
     * @param connectionFactory usine de connexion Redis
     * @return RedisTemplate configuré
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Template Redis spécialisé pour les Strings.
     * Utilisé par RateLimitService pour gérer les tokens.
     * @param connectionFactory usine de connexion Redis
     * @return StringRedisTemplate configuré
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
