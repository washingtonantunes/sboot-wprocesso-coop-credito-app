package br.com.wti.core.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Escuta eventos de invalidação publicados por outros backends via Redis Pub/Sub
 * e remove as entradas correspondentes do cache local.
 *
 * <p>Nenhum listener registrado ainda. Para adicionar um:
 *
 * <pre>
 * container.addMessageListener(
 *         evictListener("meu-cache", Long::valueOf),
 *         new ChannelTopic(CacheInvalidationChannels.MEU_CANAL));
 * </pre>
 */
@Slf4j
@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class CacheInvalidationListenerConfig {

    private final CacheManager cacheManager;

    @Bean
    RedisMessageListenerContainer cacheInvalidationContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @SuppressWarnings("unused") // auxiliar para registros futuros de listener
    private MessageListener evictListener(String cacheName, KeyConverter keyConverter) {
        return (message, pattern) -> {
            String payload = new String(message.getBody());
            Cache cache = this.cacheManager.getCache(cacheName);
            if (cache == null) {
                log.warn("Cache {} não encontrado ao processar invalidação (payload={})", cacheName, payload);
                return;
            }
            if (CacheInvalidationChannels.PAYLOAD_INVALIDAR_TUDO.equals(payload)) {
                cache.clear();
                log.debug("Cache {} invalidado completamente", cacheName);
                return;
            }
            try {
                cache.evict(keyConverter.converter(payload));
                log.debug("Cache {} invalidado: chave={}", cacheName, payload);
            } catch (RuntimeException exception) {
                log.warn(
                        "Falha ao converter payload de invalidação do cache {} (payload={}): {}",
                        cacheName,
                        payload,
                        exception.getMessage());
            }
        };
    }

    @FunctionalInterface
    private interface KeyConverter {
        Object converter(String payload);
    }
}
