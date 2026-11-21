# Estrutura do projeto

```text
br.com.wti/
├── creditorural/          # Produto Crédito Rural
│   ├── controller/        # Endpoints, DTOs e interfaces OpenAPI
│   ├── domain/            # Entidades, enums, repositórios e specifications
│   ├── mapper/            # Conversão entre entidade e DTO
│   ├── service/           # Fluxo da operação e sincronização de dados locais
│   └── util/              # Catálogo de auditoria do domínio
├── integracao/
│   └── cadastro/          # Cliente do cadastro pelo canal interno
└── core/                  # Infraestrutura transversal
    ├── auditoria/
    ├── cache/
    ├── config/
    ├── exception/
    ├── handler/
    ├── security/
    └── tenant/
```

O `core/` não importa o pacote do produto. O código de negócio vive em `br.com.wti.creditorural`; os clientes de outros serviços ficam em `integracao/<nome>/`, fora do domínio.

Existe um controller e um service para `OperacaoCreditoRural`. Listar e buscar compartilham o endpoint entre os tipos Central e Cooperativa por funcionalidades alternativas. Contratar é exclusivo da Cooperativa; aprovar, negar e desclassificar são exclusivos da Central.
