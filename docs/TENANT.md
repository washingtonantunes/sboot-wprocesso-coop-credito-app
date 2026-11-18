# Isolamento por cooperativa

O pacote `core/tenant` resolve o escopo a partir do token e é copiado sem alteração entre os backends do sistema.

| Tipo do usuário | Escopo |
|-----------------|--------|
| `COOPERATIVA` | Cooperativa do token, mesmo se a requisição informar outra |
| `CENTRAL`, sem cooperativa informada | Todas as cooperativas |
| `CENTRAL`, com cooperativa informada | Apenas a cooperativa informada |

O controller resolve o escopo com `TenantContext.escopo(...)` e o serviço o aplica na consulta. Nenhum filtro de cooperativa é lido diretamente pelo serviço para decidir o tipo do usuário.

## Entidades e referências locais

| Entidade | Tabela | Escopo |
|----------|--------|--------|
| `OperacaoCreditoRural` | `operacao_credito_rural` | Pertence à cooperativa gravada no token no momento da contratação |
| `Cooperativa` | `cooperativa` | Cópia local identificada por `id_externo` |
| `Cooperado` | `cooperado` | Cópia local identificada por `id_externo` e vinculada à cooperativa |

As colunas `cooperativa_id` e `cooperado_id` da operação guardam os identificadores externos. As relações locais são somente para leitura e fornecem os nomes das listagens.

## Endpoints e funcionalidades

Base: `/operacoes-credito-rural`.

| Ação | Endpoint | Funcionalidade |
|------|----------|----------------|
| Listar | `GET /paginado` | `CREDITO_RURAL_PAGINADO_CENTRAL` ou `CREDITO_RURAL_PAGINADO_COOPERATIVA` |
| Buscar | `GET /{id}/buscar` | `CREDITO_RURAL_BUSCAR_CENTRAL` ou `CREDITO_RURAL_BUSCAR_COOPERATIVA` |
| Contratar | `POST /contratar` | `CREDITO_RURAL_CONTRATAR` |
| Aprovar | `PUT /{id}/aprovar` | `CREDITO_RURAL_APROVAR` |
| Negar | `PUT /{id}/negar` | `CREDITO_RURAL_NEGAR` |
| Desclassificar | `PUT /{id}/desclassificar` | `CREDITO_RURAL_DESCLASSIFICAR` |

A listagem filtra por status e nome do cooperado. A Central também filtra por nome da cooperativa. O endpoint paginado aceita `cooperativaId`; o core ignora esse valor para usuário de cooperativa.

## Contratação e sincronização

A contratação recebe `cooperadoId`, valor positivo e finalidade. A cooperativa é obtida pelo escopo, nunca pelo corpo da requisição.

Antes de gravar, o serviço consulta o cooperado ativo em `GET /api/cooperativas/{cooperativaId}/cooperados/{cooperadoId}` e a cooperativa em `GET /api/cooperativas/{cooperativaId}`, pelo canal interno com token de serviço. Se qualquer consulta falhar, nenhuma operação é criada. As respostas atualizam as tabelas locais pelo identificador externo e nome.

## Regras de estado

- Toda contratação inicia em `SOLICITADA`.
- Aprovar e negar só aceitam operações `SOLICITADA`.
- Negar exige motivo e muda a situação para `NEGADA`.
- Desclassificar só aceita operações `APROVADA` e exige tipo, valor e motivo.
- Desclassificação `TOTAL` exige valor igual ao da operação; `PARCIAL` exige valor menor.
- Uma desclassificação concluída muda a situação para `DESCLASSIFICADA`.

## Testes de isolamento

`OperacaoCreditoRuralIsolamentoControllerTest` cobre listagem, busca por identificador, cooperativa informada e leitura da Central com e sem filtro. `OperacaoCreditoRuralAutorizacaoControllerTest` cobre as funcionalidades exigidas por cada endpoint e a negação da ação exclusiva da Central para usuário de cooperativa, com token e interceptor reais. `OperacaoCreditoRuralControllerTest` cobre contratação, integração com cadastro, regras de campos e transições de estado.
