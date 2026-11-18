# sboot-wprocesso-coop-credito-app

Microsserviço da área de crédito do sistema wprocesso-coop. O produto Crédito Rural registra contratações feitas pelas cooperativas, com aprovação, negativa e desclassificação pela Central.

## Domínio

O produto fica no pacote `br.com.wti.creditorural`. A operação grava o identificador da cooperativa do escopo, o identificador do cooperado, o código `CREDITO_RURAL`, o valor, a finalidade e a situação. Os dados locais de cooperativa e cooperado mantêm os nomes usados nas consultas paginadas.

Antes de contratar, o back confirma o cooperado ativo no cadastro pelo canal interno. O token de serviço é usado na chamada e a cooperativa vem do escopo do usuário.

## Tecnologias

Java 21 · Spring Boot 4.1.2 · Spring Data JPA · Hibernate Envers · MySQL · H2 · Lombok · MapStruct · SpringDoc/OpenAPI

## Autorização e escopo

Este back não tem login. Ele valida o token emitido pelo `sboot-wprocesso-coop-seguranca-app` e exige as funcionalidades do produto em cada endpoint. A cooperativa da operação sempre vem de `core/tenant`; a Central consulta operações de todas as cooperativas ou filtra por cooperativa.

## Documentação

| Documento | Conteúdo |
|-----------|----------|
| [docs/TENANT.md](docs/TENANT.md) | Isolamento, endpoints, integração com cadastro e regras de estado |
| [docs/AMBIENTE.md](docs/AMBIENTE.md) | Profiles, porta 8082, banco e Swagger |
| [docs/PROPERTIES.md](docs/PROPERTIES.md) | Properties e integração com cadastro |
| [docs/ESTRUTURA.md](docs/ESTRUTURA.md) | Pacotes do produto e core |
| [docs/PADROES.md](docs/PADROES.md) | Padrões de implementação |
| [docs/SCRIPTS_SQL.md](docs/SCRIPTS_SQL.md) | Schema, índices e fixtures |
| [docs/TESTES.md](docs/TESTES.md) | Testes de integração e isolamento |
| [docs/REGRAS.md](docs/REGRAS.md) | Regras específicas deste back |
| [docs/QUALIDADE.md](docs/QUALIDADE.md) | Formatação e regras estáticas |
| [docs/CACHE.md](docs/CACHE.md) | Cache e integração back-for-back |

## Como subir com H2

Com o `sboot-wprocesso-coop-cadastro-app` e o `sboot-wprocesso-coop-seguranca-app` disponíveis:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

A API fica em `http://localhost:8082/sboot-wprocesso-coop-credito-app`. Swagger em `/swagger-ui/index.html` e console H2 em `/h2-console`.
