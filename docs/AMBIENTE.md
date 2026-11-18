# Ambiente e execução

## Tecnologia e coordenadas

- Java 21, Spring Boot 4.1.2, Spring Data JPA, Hibernate Envers, MySQL e H2.
- Group ID: `br.com.wti`.
- Artifact ID: `sboot-wprocesso-coop-credito-app`.

## Porta e contexto

O serviço usa a porta 8082 e o contexto `/sboot-wprocesso-coop-credito-app` em todos os profiles.

## Profiles

| Profile | Banco | `ddl-auto` |
|---------|-------|------------|
| padrão | MySQL provisionado | `validate` |
| `local` | MySQL local | `validate` |
| `docker` | MySQL em container | `validate` |
| `h2` | H2 em memória | `none` |
| `test` | H2 em memória por contexto | `none` |

Os profiles `h2` e `test` executam o schema MySQL em modo de compatibilidade com H2. O profile `test` limpa e recria as tabelas antes dos testes.

## Integrações

O serviço consulta o cadastro em `app.cadastro.endpoint`. O profile H2 e local usam `http://localhost:8081/sboot-wprocesso-coop-cadastro-app`; Docker usa o host `cadastro`. O token do serviço e o back chamado compartilham `jwt.interno.secret`.

## Execução local

Subir o serviço com H2:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Console H2: `http://localhost:8082/sboot-wprocesso-coop-credito-app/h2-console`.

Swagger: `http://localhost:8082/sboot-wprocesso-coop-credito-app/swagger-ui/index.html`.
