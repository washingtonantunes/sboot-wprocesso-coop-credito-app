package br.com.wti.core.cache;

/**
 * Canais Redis Pub/Sub usados por backends donos para avisar que um dado mudou.
 *
 * <p>Cada canal é uma constante desta classe, com a mesma string no backend dono.
 *
 * <p>Convenção de payload: identificador do agregado alterado como string, ou
 * {@code "*"} para invalidação total.
 */
public final class CacheInvalidationChannels {

    public static final String PAYLOAD_INVALIDAR_TUDO = "*";

    private CacheInvalidationChannels() {}
}
