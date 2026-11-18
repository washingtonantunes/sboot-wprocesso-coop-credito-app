# Padrões de implementação

## Formatação e legibilidade

- Não quebrar linha na inicialização simples de variável quando a expressão couber em uma linha:

```java
EntidadeResponseDTO response = new EntidadeResponseDTO();
List<EntidadeProcesso> entidades = new ArrayList<>();
```

- Quebrar linha somente quando a expressão exceder o limite de legibilidade, possuir vários argumentos ou contiver encadeamento relevante:

```java
SolicitacaoProcesso solicitacao =
        new SolicitacaoProcesso(
                dto.getId(),
                this.entidadeProcessoMapper.toEntidadesProcesso(dto.getEntidades()),
                PerfilProcesso.PADRAO);
```

- Em serviços que orquestram conversão, processamento e montagem de resposta, declarar variáveis intermediárias com nomes do domínio. Evitar encadear as três etapas em um único `return`:

```java
SolicitacaoProcesso solicitacao = this.entidadeProcessoMapper.toSolicitacaoProcesso(dto);

ResultadoProcesso resultado = this.entidadeProcesso.executar(solicitacao);

return this.entidadeApiAssembler.montar(resultado);
```

- Para builders, manter chamadas com argumento simples na mesma linha. Quebrar somente quando houver múltiplos argumentos, expressão longa ou a separação melhorar a leitura:

```java
.quantidadeMinima(dadosGerais.getQuantidadeMinima())
.flagAtivo(dadosGerais.getFlagAtivo())
```

## DTOs do pacote `controller/dto/`

Usados pelos endpoints REST expostos ao frontend. Ficam **obrigatoriamente** dentro de `controller/dto/`, separados em dois subpacotes:

```
controller/
└── dto/
    ├── request/    # Adicionar*, Editar*, Filtro*
    └── response/   # *ResponseDTO, *PaginadoResponseDTO
```

### `controller/dto/response/` — Response

- Classe: apenas `@Getter` + `@Setter`
- Campos obrigatórios (NOT NULL na entidade): `@NotNull`
- Campos `String` obrigatórios: também `@NotNull` (no response não se usa `@NotBlank`)
- Campos opcionais: sem anotação de validação

```java
package br.com.wti.creditorural.controller.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntidadeResponseDTO {

    @NotNull
    private Long id;

    @NotNull
    private String atributoStringObrigatorio;

    @NotNull
    private Integer atributoIntObrigatorio;

    @NotNull
    private TipoEnum atributoEnumObrigatorio;

    private String atributoStringOpcional;
}
```

### `controller/dto/request/` — Request

- Classe: `@Getter` + `@Setter` + `@JsonInclude(Include.NON_NULL)`
- Campos `String` obrigatórios: `@NotBlank`
- Campos de outros tipos obrigatórios (`Long`, `Integer`, enum, etc.): `@NotNull`
- Objetos aninhados obrigatórios: `@NotNull` + `@Valid` (propaga validação recursivamente)
- Campos opcionais: sem anotação de validação

```java
package br.com.wti.creditorural.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class EditarEntidadeRequestDTO {

    @NotBlank
    private String atributoStringObrigatorio;

    @NotNull
    private Long atributoLongObrigatorio;

    @NotNull
    private TipoEnum atributoEnumObrigatorio;

    @NotNull
    @Valid
    private EditarSubEntidadeRequestDTO atributoObjetoObrigatorio;

    private String atributoStringOpcional;
}
```

### Resumo das anotações — `controller/dto/`

| Situação | Anotação |
|----------|----------|
| `String` obrigatória em **request** | `@NotBlank` |
| Qualquer outro tipo obrigatório em request ou response | `@NotNull` |
| Objeto aninhado obrigatório em **request** | `@NotNull` + `@Valid` |
| Campo opcional | *(sem anotação)* |
| Classe de response | `@Getter` + `@Setter` |
| Classe de request | `@Getter` + `@Setter` + `@JsonInclude(Include.NON_NULL)` |

> Este projeto não expõe DTOs de integração (`api/dto/`) para outros microsserviços — é consumido via cliente HTTP direto ou via mecanismos de segurança do Spring Security.

---

## Controller

- `@RestController` + `@RequestMapping("/entidades")` + `@RequiredArgsConstructor`
- Implementa interface `*ControllerApi` do pacote `controller/openapi/` — Swagger na interface, não no controller
- **Zero lógica** — apenas delega ao service e retorna o resultado
- `@FuncionalidadeAutorizada` em cada endpoint. Ação que serve à Central e à cooperativa aceita uma funcionalidade de cada tipo, `@FuncionalidadeAutorizada({"X_CENTRAL", "X_COOPERATIVA"})`; ação exclusiva de um tipo aceita só a dele, sem sufixo
- Um controller por entidade, sem prefixo de tipo de usuário no caminho
- Em dado que pertence a uma cooperativa, obtém o escopo com `tenant.escopo(...)` e o repassa ao service; não consulta o tipo do usuário (ver [TENANT.md](TENANT.md))
- `produces = MediaType.APPLICATION_JSON_VALUE` em todos os endpoints
- Params: `@Valid @ModelAttribute` para filtros, `@RequestBody` para POST/PUT, `@PathVariable` para IDs

## OpenAPI interface (`controller/openapi/*ControllerApi.java`)

- Interface com `@Tag`, `@Operation`, `@ApiResponse` — o controller implementa essa interface

## Mapper (`mapper/`)

- Interface com `@Mapper` — sem `componentModel` (herdado do argumento global `-Amapstruct.defaultComponentModel=spring` no `pom.xml`)
- **Uma interface por entidade** — nenhum mapper contém métodos de outras entidades; se precisar mapear um tipo aninhado, delegar via `@Mapper(uses = {OutroMapper.class})`
- **Ordem obrigatória dos métodos**: métodos que retornam **entidade** primeiro, depois métodos que retornam **DTO**
- **Convenção de nomes**:
    - Retorna entidade → `to<NomeDaEntidade>(DTO dto)` — ex: `toCooperativa`, `toCooperado`
    - Retorna DTO → `to<NomeCompletoDoDTO>(Entidade e)` — ex: `toCooperativaResponseDTO`, `toCooperadoResponseDTO`
- Separar seções com comentários `// ==================== Entidade ====================` e `// ==================== DTO ====================`
- Usar `@Mapping(target = "campo", ignore = true)` para campos que não existem na fonte ou são preenchidos manualmente após o mapeamento
- Conversões de tipo incompatível (ex: `BigDecimal` → `Long`, `Object` → `String`) via `expression = "java(...)"`
- Enums incompatíveis entre source e target → `@Mapping(target = "status", ignore = true)` e definir manualmente
- **Nunca usar** `toEntity` ou `toResponseDTO` como nome — sempre incluir o nome da entidade/DTO
- **Nome de registro relacionado em listagem** vem no `*PaginadoResponseDTO` em `<entidade>Nome`, preenchido por navegação (`@Mapping(target = "cooperativaSuperiorNome", source = "cooperativaSuperior.nome")`). O relacionamento é um `@ManyToOne` só de leitura (`insertable=false, updatable=false`) sobre a mesma coluna do identificador, marcado `@NotAudited` para não duplicar a coluna no Envers; a coluna `Long` continua a que grava e a que o escopo usa. A consulta paginada o carrega junto com `@EntityGraph(attributePaths = {...})` no `findAll(Specification, Pageable)`, sem uma consulta por linha.

## Service

- `@Service` + `@RequiredArgsConstructor`
- MapStruct para conversão entity ↔ DTO via mapper injetado (`@Mapper` com `componentModel=spring` global)
- `Specification` + repository para buscas filtradas → retorna `PageResponseDTO<*>`
- `@Transactional` **apenas em operações de escrita**
- **Nunca** implementa `findBy + orElseThrow` inline nem tem método `verificar<X>` próprio — sempre injeta e chama o `<Entidade>LookupService` correspondente (ver seção abaixo).
- Recebe o `EscopoCooperativa` pronto do controller e o repassa a specifications e lookups; não consulta o tipo do usuário para decidir filtro.
- Para buscar entidade relacionada: chama `<outroEntidade>LookupService.verificar*()` — nunca acessa repositório alheio.

## Lookup services (`service/lookup/`)

Toda consulta que retorna uma entidade (ou Projection) pela chave e lança `EntidadeNaoEncontradaException` quando não encontra mora em uma classe `<Entidade>LookupService` no pacote `service.lookup/`.

**Regras:**

- Injeta **apenas o Repository**. Sem `SecurityService`, sem outros services, sem validators, sem mappers.
- Métodos `public <Entidade> verificar<X>(chave)` fazem `findBy...().orElseThrow(() -> EntidadeNaoEncontradaException.createWith("..."));` e nada mais.
- **Nada de regra de negócio.** Validação continua no `<Entidade>Validator`; Lookup e Validator são separados.
- Se o lookup depende de contexto externo (ex: identificador do usuário autenticado), esse valor vem **como parâmetro do método** — o Lookup não injeta `SecurityService` nem consulta contexto de request. Quem chama resolve o contexto e passa o valor.
- Em entidade que pertence a uma cooperativa, o método recebe também o `EscopoCooperativa` e lança `EntidadeNaoEncontradaException` quando o registro está fora dele (`verificarCooperado(id, escopo)`).
- Projection (interface `SELECT ...` do Spring Data) também mora no Lookup se for lookup por chave com `orElseThrow`.

**Exemplo:**

```java
@Service
@RequiredArgsConstructor
public class EntidadeLookupService {

    private final EntidadeRepository entidadeRepository;

    public Entidade verificarEntidade(Long id) {
        return this.entidadeRepository.findById(id)
                .orElseThrow(() -> EntidadeNaoEncontradaException.createWith("entidade.nao.encontrada"));
    }
}
```

**Convenção de nome do método:**

- `verificar<Entidade>(id)` — busca por id
- `verificar<Entidade>Por<Chave>(chave)` — busca por outra chave
- `verificar<Entidade>ParaAtualizacao(id)` — consulta com lock pessimista
- `verificar<Entidade><Contexto>Projection...(chave)` — retorna Projection em vez de entidade

**Não vai pro Lookup:**

- `buscarOuCriar` (cria também, não é lookup puro)
- Queries que lançam `BusinessException` (regra de negócio, não "não encontrado")
- Queries que lançam `AcessoNegadoException` (autorização)
- Lookups com key custom pra mascarar existência (ex: buscar uma entidade mas lançar erro com key de outra pra não vazar cadastro) — ficam inline no Service, pois desviam do padrão do Lookup

**Exemplo de consumo (Service normal):**

```java
@Service
@RequiredArgsConstructor
public class OperacaoService {

    private final OperacaoRepository operacaoRepository;
    private final EntidadeLookupService entidadeLookupService;
    private final OperacaoMapper operacaoMapper;

    @Transactional
    public OperacaoResponseDTO adicionarOperacao(AdicionarOperacaoRequestDTO dto) {
        Entidade entidade = this.entidadeLookupService.verificarEntidade(dto.getEntidadeId());
        // ...
    }
}
```

## Validator (`validator/`)

- `@Component` + `@RequiredArgsConstructor`, um por entidade (`CooperativaValidator`, `CooperadoValidator`)
- Métodos `validar<Regra>(...)` sem retorno, que lançam `BusinessException.createWith("chave")` quando a regra é violada
- `DocumentoValidator` confere formato e dígitos verificadores de CPF e CNPJ e devolve booleano; quem decide a mensagem é o validator da entidade
- Formato simples de campo (obrigatório, tamanho, e-mail, telefone) fica nas anotações do DTO de request

## Exceptions

- `EntidadeNaoEncontradaException` → HTTP 404 (entidade não existe)
- `BusinessException` → HTTP 400, com `tipoErro` igual a `NEGOCIO` (regra de negócio violada)
- `AcessoNegadoException` → HTTP 403
- `IntegracaoException` → HTTP 502 (falha na chamada a outro back)
- Em `orElseThrow`: `.createWith("chave")`
- Mensagens em `src/main/resources/messages.properties`, chave: `entidade.situacao`

## Entity

- `@Audited` (Hibernate Envers — auditoria automática de alterações)
- Lombok: `@Getter @Setter @NoArgsConstructor @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)`
- `@Entity @Table(name = "nome_tabela")`
- ID: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) @EqualsAndHashCode.Include`
- Enums: `@Enumerated(EnumType.STRING)`
- Campo Status: sempre `StatusEnum` (ATIVO/INATIVO)
- Índices e constraints únicas: definidos somente nos scripts de schema em `src/main/resources/db/schema/mysql/`

### Concorrência

- Usar `@Lock(LockModeType.PESSIMISTIC_WRITE)` somente em consulta de repositório chamada dentro de método `@Transactional` de escrita.
- Aplicar o lock quando a regra depende do estado atual de um agregado e duas requisições simultâneas poderiam aprovar a mesma condição.
- Não usar lock pessimista para substituir chave única, validação de DTO ou atualização simples por ID.
- Nomear explicitamente métodos com lock, por exemplo `findByIdForUpdate` ou `findByProdutoIdForUpdate`.

## Repository — queries agregadas (`GROUP BY`, múltiplas colunas)

- **Nunca** retornar `List<Object[]>` — usar VO com constructor expression JPQL
- VO fica em `domain/repository/vo/`, nome `<Contexto><Finalidade>VO`, classe `@Getter @AllArgsConstructor` só com os campos (sem lógica)
- Query usa `SELECT new br.com.wti.creditorural.domain.repository.vo.NomeVO(campo1, campo2, ...) FROM ...`
- Consumidor usa os getters do VO diretamente — nunca cast por índice de array (`row[0]`, `row[1]`)
- Exceção: queries via Hibernate Envers (`AuditReader`/`AuditQuery`) continuam em `Object[]` — a API do Envers não aceita constructor expression JPQL

## Logs de exception

O `LoggingAspect` em `core/aspect/LoggingAspect.java` captura `@AfterThrowing` em qualquer bean anotado com `@Service`, `@Repository` ou `@RestController` e registra a exception com stacktrace. Há dedup por instância para evitar log duplicado quando propaga.

- Em classe `@Service` / `@Repository` / `@RestController`, **não** logar a exception no `catch` — o aspect já loga. Só manter o `catch` quando houver lógica (ex.: wrap em `IntegracaoException`, atualizar status).
- Em classe `@Component` que é topo de pilha (`scheduler`, `processor`, state-machine `action`), logar com a exception completa como último argumento:

```java
log.error("Erro ao processar X {}", id, e);
```

- Nunca usar o padrão redundante `("...: {}", id, e.getMessage(), e)` — o próprio `e` já carrega a mensagem. Formato correto é `("... {}", id, e)`.
- Nunca passar só `e.getMessage()` em log de exception — perde stacktrace.
