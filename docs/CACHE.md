# Cache

O `core/` oferece cache back-for-back via Redis e invalidação por Pub/Sub. Este back ainda não aplica cache às consultas de cadastro: o cadastro confirma o cooperado e atualiza as tabelas locais durante a contratação.

- Prefixo Redis: `wprocesso-coop-credito::` (`app.cache.prefixo`).
- TTL padrão do core: 15 minutos.
- Configuração: `br.com.wti.core.config.CacheConfig`.
- Listener: `br.com.wti.core.cache.CacheInvalidationListenerConfig`.

Uma futura chamada cacheada precisa ter canal de invalidação no serviço dono. Dados por cooperativa precisam incluir a cooperativa na chave.
