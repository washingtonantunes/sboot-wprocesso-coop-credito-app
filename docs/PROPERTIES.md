# Padrão de organização dos `application.properties`

## Princípio

**O `application.properties` base é a fonte da verdade.** Perfis (`local`, `h2`, `docker`, `test`) sobrescrevem apenas o que diverge. Chave ausente no perfil é herdada do base — o Spring Boot resolve isso em runtime; nada é copiado em build.

O perfil `test` mora em `src/test/resources/application-test.properties` e é ativado pela `TestBase` via `@ActiveProfiles("test")`. O base de `src/main/resources` também entra no classpath de teste, então as regras abaixo valem igual.

Vale para toda flag de infraestrutura, incluindo `@ConditionalOnProperty` e `@Value`.

## Como o Spring resolve

Em runtime, o Spring Boot carrega os `.properties` como `PropertySource` em ordem de precedência:

1. `application-<profile>.properties` (perfil ativo) — maior precedência.
2. `application.properties` — base, sempre carregado.
3. Default no código (`@Value("${chave:default}")` ou `@ConditionalOnProperty(matchIfMissing = true)`).

Nenhum arquivo é combinado em disco. O `.jar` final embute cada `.properties` como está. Rodar com `--spring.profiles.active=local` acopla o perfil ao base; chaves faltando no perfil caem no base automaticamente.

## Regras

1. **Toda flag `*.scheduler.enabled` e similar aparece explicitamente no `application.properties` base** com seu valor de produção. Nunca depender de `matchIfMissing=true` para ligar algo que vale em produção.
2. **Nos perfis, só aparece o que diverge do base.** Repetir valor igual é poluição e induz erro na manutenção (muda em um, esquece no outro).
3. **Chaves de duração usam `Duration` do Spring**, sem unidade no nome. Aceita `10m`, `PT10M`, `30s`, `2h`, `500ms`.
   - Bom: `foo.timeout=10m` + `@Value("${foo.timeout}") Duration timeout`
   - Ruim: `foo.timeout-minutos=10` + `long timeoutMinutos`
4. **Default no `@Value` é opcional** quando a chave existe no base — a chave sempre resolve. Se preferir deixar como "cinto e suspensório", OK, mas o base continua sendo a fonte da verdade.
5. **Comentar o perfil** com um cabeçalho dizendo que ele só sobrescreve o que diverge, para quem lê não procurar o que não está lá.
6. **Dados sensíveis e configurações específicas de ambiente usam variável de ambiente no base** (`${VAR_NAME}`, sem default). Perfis `local`, `h2`, `docker`, `test` sobrescrevem com valor literal para dev/teste rodar sem precisar setar env. Vale para: credenciais de BD, segredos JWT, URLs de integrações, diretórios de upload, qualquer coisa que mude entre ambientes de prod.

## Agrupamento por bloco

Dentro de cada arquivo, agrupe chaves do mesmo contexto num bloco com cabeçalho curto. Preferir **vários blocos curtos** a um bloco grande "SCHEDULER" com tudo misturado.

```properties
#SCHEDULER - <nome> (<o que faz>)
<prefixo>.scheduler.enabled=true
<prefixo>.executor.corePoolSize=5
<prefixo>.executor.maxPoolSize=10
<prefixo>.executor.queueCapacity=100

#SCHEDULER - Reaper (<o que faz>)
<prefixo>.reaper.scheduler.enabled=true
<prefixo>.reaper.timeout=10m
```

## Estrutura esperada do cabeçalho

**`application.properties`**

```properties
# ======================================================================
# Properties base (sempre carregado). Valores de PRODUCAO.
# Perfis (local, h2, docker, test) sobrescrevem APENAS o que diverge.
# Ver docs/PROPERTIES.md para o padrao de organizacao.
# ======================================================================
```

**`application-<perfil>.properties`**

```properties
# ======================================================================
# Perfil <PERFIL>. Apenas chaves que DIVERGEM de application.properties base.
# Chaves ausentes aqui sao herdadas do base. Ver docs/PROPERTIES.md.
# ======================================================================
```

## Como adicionar nova flag de scheduler/infra

1. Adicione a chave em `application.properties` com o valor de produção.
2. Se algum perfil (local, h2, docker, test) precisa de valor diferente, adicione **só nesse perfil** com o valor divergente.
3. No código, use `@Value("${chave}")` sem default (o base garante presença).
4. Para flags booleanas de ligar/desligar component, `@ConditionalOnProperty(name = "…", havingValue = "true", matchIfMissing = true)` continua sendo rede de segurança — mas a chave no base é a documentação viva.
5. Atualize este arquivo adicionando a nova chave na tabela "Schedulers deste projeto" ou "Variáveis de ambiente" abaixo.

---

# Específico do credito-app

## Schedulers deste projeto

_Este projeto não possui schedulers._

## Outras chaves de infra

| Chave | Descrição |
|-------|-----------|
| `jwt.secret` | Chave que valida o token do usuário. É a mesma do back que emite o token; este back não tem login |
| `jwt.expiracao` | TTL usado pelo `core` ao gerar token (`Duration`, ex: `24h`) |
| `jwt.interno.secret` | Chave do canal interno (back-for-back, endpoints `/api/**`). O `JwtServicoInternoService` valida com ela o token de serviço dos backs que chamam; precisa ser igual à deles |
| `app.cadastro.endpoint` | URL base do `sboot-wprocesso-coop-cadastro-app` |
| `app.cadastro.timeout` | Tempo máximo de resposta do cadastro (`Duration`) |
| `app.cadastro.max-response-size` | Tamanho máximo da resposta do cadastro |
| `app.cache.prefixo` | Prefixo das chaves deste back no Redis |

## Variáveis de ambiente

Setar em produção: `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET` (igual ao back de segurança), `JWT_INTERNO_SECRET` (igual ao cadastro) e `CADASTRO_URL`.
