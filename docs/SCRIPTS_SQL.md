# Scripts SQL

## Schema inicial

Os scripts MySQL são a fonte do schema completo. Execute-os em ordem:

| Ordem | Arquivo | Conteúdo |
|-------|---------|----------|
| 1 | `00-criar-database.sql` | Cria e seleciona `coop_credito` |
| 2 | `01-criar-schema.sql` | Tabelas locais, operação, auditoria e constraints |
| 3 | `02-criar-indices.sql` | Índices das consultas paginadas |

O script `db/schema/h2/03-criar-sequencia-revisao.sql` cria a sequência do Envers no H2. `00-limpar-schema.sql` é usado pelos testes antes da criação do schema.

## Fixtures

| Arquivo | Uso |
|---------|-----|
| `data-local.sql` | Dados de demonstração no profile local |
| `data-h2.sql` | Dados de demonstração no profile H2 |
| `src/test/resources/queries/data.sql` | Cenários de teste por método |
| `src/test/resources/queries/clear.sql` | Limpa registros de domínio na ordem reversa das FKs |

Os testes carregam operações de duas cooperativas, incluindo casos `SOLICITADA` e `APROVADA` para isolamento e transições de estado.
