package br.com.wti.core.config;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@EnableCaching
@Configuration
public class CacheConfig implements CachingConfigurer {

    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    @Value("${app.cache.prefixo}")
    private String prefixo;

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(JsonMapper jsonMapper) {
        GenericJacksonJsonRedisSerializer serializer = GenericJacksonJsonRedisSerializer.builder(jsonMapper::rebuild)
                .enableUnsafeDefaultTyping()
                .build();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(CACHE_TTL)
                .prefixCacheNameWith(prefixo)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    @Bean
    @Primary
    @Profile("!local")
    CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory, RedisCacheConfiguration redisCacheConfiguration) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .transactionAware()
                .build();
    }

    @Bean
    @Primary
    @Profile("local")
    CacheManager localCacheManager(
            RedisConnectionFactory redisConnectionFactory, RedisCacheConfiguration redisCacheConfiguration) {
        if (!redisDisponivel(redisConnectionFactory)) {
            log.warn("Redis indisponível no profile local. Utilizando cache em memória.");
            return new TransactionAwareCacheManagerProxy(new ConcurrentMapCacheManager());
        }

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .transactionAware()
                .build();
    }

    /**
     * Faz operações do cache tolerarem queda do Redis em runtime: loga e segue.
     * Leitura falha → o método @Cacheable executa normalmente (fallback).
     * Escrita/evict/clear falha → não derruba a requisição; cache fica stale até o TTL.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Falha ao ler cache {} (chave={}): {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Falha ao gravar cache {} (chave={}): {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Falha ao invalidar cache {} (chave={}): {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Falha ao limpar cache {}: {}", cache.getName(), exception.getMessage());
            }
        };
    }

    private boolean redisDisponivel(RedisConnectionFactory redisConnectionFactory) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            return connection.ping() != null;
        } catch (RuntimeException exception) {
            log.debug("Falha ao conectar ao Redis no profile local.", exception);
            return false;
        }
    }
}
