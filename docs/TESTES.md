# Padrões de testes dos backends

Padrões de teste deste projeto.

Os nomes usados nos exemplos são intencionalmente genéricos e devem ser adaptados ao domínio de cada projeto.

## Abordagem e princípio geral

O projeto utiliza **testes de integração orientados ao controller**, também chamados de testes de API HTTP, seguindo a abordagem **outside-in**. Eles validam as regras de negócio por meio do contrato HTTP público com `MockMvc` e a infraestrutura real da aplicação, mantendo as integrações externas mockadas. Portanto, não são testes unitários de controller nem testes end-to-end completos.

Os testes devem validar o fluxo da aplicação a partir de um ponto de entrada real:

- controllers HTTP com `MockMvc`;
- APIs disponibilizadas pelo projeto;
- processors acionados da mesma forma que no fluxo produtivo, quando não houver endpoint para iniciar o processamento.

Não devem ser criados testes isolados de service, repository, mapper ou outras classes internas apenas para aumentar a cobertura. Uma regra interna deve ser exercitada pelo ponto de entrada do fluxo que a utiliza.

## Estrutura

```
src/test/java/br/com/wti/creditorural/
├── api/
│   ├── ApiTestBase.java
│   └── *ApiTest.java
└── controller/
    ├── ControllerTestBase.java
    └── *ControllerTest.java
```

## Classe base

Cada projeto deve possuir uma única classe `TestBase`, fora dos pacotes `controller`, `api` e `processor`.

Local esperado:

```text
src/test/java/br/com/wti/TestBase.java
```

Controllers, APIs e processors devem estender essa mesma classe:

```java
public class RecursoControllerTest extends TestBase {
}
```

A `TestBase` concentra a configuração compartilhada:

- `@SpringBootTest`;
- `@AutoConfigureMockMvc`;
- perfil de teste;
- scripts SQL de limpeza e carga;
- `MockMvc`;
- `ObjectMapper`;
- mocks de autenticação e autorização;
- usuários autenticados utilizados pelos testes;
- métodos `carregarPayload` e `carregarMock`.

Não devem existir bases separadas como `ControllerTestBase`, `ApiTestBase` ou `ProcessorTestBase` quando elas repetirem essa mesma infraestrutura.

### Anotações obrigatórias

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SqlGroup({
    @Sql(value = "/queries/clear.sql", executionPhase = BEFORE_TEST_METHOD),
    @Sql(value = "/queries/data.sql",  executionPhase = BEFORE_TEST_METHOD)
})
```

### Banco em memória por contexto

Cada classe de teste que redefine os mocks sobe o seu próprio contexto Spring, e o H2 em memória é compartilhado pelo nome do banco. Com um nome fixo, dois contextos apontariam para o mesmo banco e as tabelas de auditoria do Envers colidiriam na chave primária de `revinfo`. Por isso a `spring.datasource.url` usa um nome único por contexto, gerado com `${random.uuid}`, que o Spring resolve uma vez para cada contexto ao montar o `DataSource`:

```properties
spring.datasource.url=jdbc:h2:mem:${random.uuid};DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;DATABASE_TO_LOWER=TRUE
```

O `clear.sql` limpa apenas as tabelas de domínio e não apaga as tabelas de auditoria.

### Helpers de autenticação disponíveis na TestBase

Para o caminho mockado (o interceptor sempre libera; define-se apenas o tipo e a cooperativa do usuário):

- `mockCentral()`
- `mockCooperativa(cooperativaId)`

Para os cenários que exercem a autorização verdadeira (a classe ativa o interceptor real em um `@BeforeEach`, como em `usarAutorizacaoReal()`), tokens reais já prontos com o prefixo `Bearer`:

- `tokenCentral(funcionalidades)`
- `tokenCooperativa(cooperativaId, funcionalidades)`
- `tokenServico()` — token de serviço do canal interno (`/api/**`)

### Testes de isolamento

Todo endpoint que lê ou grava dado de cooperativa entra nos testes de isolamento, com os cenários mínimos:

1. Usuário da cooperativa A lista e recebe apenas registros da A, havendo registros da B na base.
2. Usuário da cooperativa A busca por id um registro da B e recebe não encontrado (404), nunca acesso negado.
3. Usuário da cooperativa A filtra pelo nome da cooperativa B e não recebe nada, porque o escopo o limita à própria.
4. Usuário de cooperativa chama ação exclusiva da Central e recebe acesso negado (403). Este cenário usa token e interceptor reais; ver `CooperadoAutorizacaoControllerTest`.
5. Usuário da Central lista sem filtro de cooperativa e recebe registros de todas; filtrando pelo nome de uma, recebe apenas os dela.

## Padrão de teste

- `@DisplayName` obrigatório na classe e em cada método
- Nomes em português: `deveRetornar...`, `devePesquisar...`, `deveLancarExcecao...`
- IDs como constantes: `private final static String ID_VALIDO = "1"`, `ID_INVALIDO = "999"`
- Testar sempre: caminho feliz + ID inválido + campos obrigatórios
- Payloads JSON em `src/test/resources/payload/{entidade}/nome-cenario.json`
- Lembrar de rodar `mvnw test` após qualquer alteração

## Teste de exceções

Ao testar cenários de erro (`EntidadeNaoEncontradaException`, `BusinessException`, etc.), sempre:

- Declarar uma variável `mensagem` com o texto esperado no topo do método
- Validar `$.mensagemUsuario` com o valor da variável (texto exato que o usuário verá)
- Validar `$.mensagemDesenvolvedor` com `.isNotEmpty()` (garante que existe, sem acoplar ao texto técnico interno)
- Enviar `Accept-Language: pt-BR` para forçar o idioma da mensagem

```java
@Test
@DisplayName("Deve lançar exceção EntidadeNaoEncontradaException quando for buscar recurso inexistente")
void deveLancarExcecaoEntidadeNaoEncontradaExceptionQuandoTentarBuscarRecursoInexistente() throws Exception {

    var mensagem = "Recurso não encontrado";

    this.mockMvc.perform(get(RECURSO_URL + "/" + RECURSO_ID_INEXISTENTE)
                    .header("Accept-Language", "pt-BR"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensagemUsuario").value(mensagem))
            .andExpect(jsonPath("$.mensagemDesenvolvedor").isNotEmpty());
}
```

## Payloads das requisições

Payloads HTTP devem ficar em arquivos JSON dentro de:

```text
src/test/resources/payload/
```

O teste deve carregar o conteúdo com `carregarPayload`:

```java
String json = carregarPayload("/payload/recurso/criar_recurso.json");

this.mockMvc.perform(post("/recursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isOk());
```

Evite montar DTOs ou grandes estruturas de request diretamente na classe de teste.

Valores variáveis podem ser representados por marcadores no JSON e substituídos no teste:

```java
String json = carregarPayload("/payload/recurso/criar_recurso.json");
json = json.replace("#dataInicioVigencia", LocalDate.now().toString());
```

## Mocks de integrações externas

As integrações externas devem continuar sendo mocks com Mockito. Não é necessário subir WireMock ou outro servidor HTTP local quando o objetivo é validar apenas as regras internas do projeto.

Cada projeto é responsável por suas próprias regras. Erros recebidos de outro serviço podem ser simulados diretamente no client, inclusive quando a aplicação apenas repassa a exceção ao chamador.

As respostas das integrações devem ficar em:

```text
src/test/resources/integracao/
```

Use `carregarMock` para desserializar o JSON no DTO correspondente:

```java
RespostaIntegracaoApiResponseDTO respostaIntegracaoApiResponseDTO = carregarMock(
        "/integracao/servico/resposta-consulta.json",
        RespostaIntegracaoApiResponseDTO.class);

when(this.integracaoClient.consultar(any()))
        .thenReturn(respostaIntegracaoApiResponseDTO);
```

O objeto deve ser declarado antes do `when`. Isso facilita a inspeção do retorno durante o debug.

Evite:

```java
when(this.integracaoClient.consultar(any())).thenReturn(carregarMock(
        "/integracao/servico/resposta-consulta.json",
        RespostaIntegracaoApiResponseDTO.class));
```

Quando a mesma fixture for utilizada em vários testes, crie um método de mock com nome representativo:

```java
private void mockRespostaIntegracao() {
    RespostaIntegracaoApiResponseDTO respostaIntegracaoApiResponseDTO = carregarMock(
            "/integracao/servico/resposta-consulta.json",
            RespostaIntegracaoApiResponseDTO.class);

    when(this.integracaoClient.consultar(any()))
            .thenReturn(respostaIntegracaoApiResponseDTO);
}
```

Objetos pequenos também devem ser carregados de fixtures quando representarem respostas de integrações.

## Entidade inexistente e valor inválido

Use **inexistente** quando o identificador possui formato válido, mas não corresponde a um registro persistido:

```java
private static final String RECURSO_ID_INEXISTENTE = "999";
```

Exemplos:

- recurso principal inexistente;
- recurso relacionado inexistente;
- vínculo inexistente.

Use **inválido** somente quando o próprio valor viola uma regra de formato ou de negócio:

- CPF ou CNPJ inválido;
- status inválido;
- percentual fora do limite;
- modalidade inválida;
- data ou vigência inválida;
- campo obrigatório ausente;
- valor de campo dinâmico incompatível com seu tipo.

Essa distinção deve aparecer no nome do método, no `@DisplayName`, nas constantes e no nome do arquivo de payload.

## Validação por endpoints GET

Depois de executar uma operação, prefira consultar o resultado pelo endpoint GET do próprio projeto:

```java
this.mockMvc.perform(get("/recursos/{id}/buscar", recursoId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(recursoId));
```

Isso também valida:

- controller;
- autorização;
- service;
- consulta;
- mapper;
- serialização;
- contrato da resposta.

Evite usar diretamente um repository apenas para verificar um dado que já está disponível em um endpoint.

O uso de repository no teste continua aceitável quando necessário para:

- preparar o cenário;
- descobrir um identificador gerado que não pode ser obtido pela resposta;
- aguardar a conclusão de processamento assíncrono;
- validar estado interno que não é exposto por nenhuma API;
- consultar detalhes técnicos de fases ou relacionamentos internos.

Não force uma substituição por GET quando o contrato público não expõe a informação necessária.

## Processamentos assíncronos

Não use tempos fixos com `Thread.sleep`. Aguarde uma condição observável com Awaitility:

```java
await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(200))
        .until(() -> processamentoConcluido(transmissaoId));
```

Após a conclusão, valide o resultado pelo endpoint GET quando ele estiver disponível.

## Autenticação e autorização

Quando o projeto possuir autenticação ou autorização, configure explicitamente o perfil exigido pelo cenário utilizando os helpers disponíveis na `TestBase`.

Ao consultar um recurso filtrado por um identificador de acesso, use o identificador do beneficiário ou proprietário do recurso. Para cenários de acesso negado, prefira nomes como `IDENTIFICADOR_SEM_ACESSO` em vez de `IDENTIFICADOR_INVALIDO`, pois o valor pode ser válido e apenas não possuir acesso ao registro.

## Organização e legibilidade

- Declare o payload próximo da chamada HTTP.
- Declare respostas mockadas antes do respectivo `when`.
- Use nomes que descrevam a condição e o resultado esperado.
- Centralize somente fixtures ou configurações realmente repetidas.
- Preserve no teste as informações relevantes para investigação durante o debug.
- Não esconda toda a preparação em abstrações genéricas difíceis de rastrear.
- Mantenha cada teste independente dos demais.

## Preferência de teste

- Priorizar `ControllerTest` e `ProcessorTest` para validar fluxo; evitar testes puros de DTO, repository e service, salvo pedido explícito.
- Não deve criar métodos em repository para ser usado em testes; criar um método com `Specification` na própria classe de teste.
- Ao editar arquivo textual sensível, preservar encoding e newline existentes; não regravar o arquivo inteiro nem normalizar caracteres especiais.

## Cobertura JaCoCo

A cobertura deve orientar a localização de fluxos ainda não exercitados, e não a criação de testes artificiais.

Ao cobrir uma linha ou branch ausente:

1. identifique o fluxo público que alcança a regra;
2. crie o cenário a partir desse ponto de entrada;
3. utilize os mesmos padrões de payload, integração e autenticação;
4. valide o comportamento observável do fluxo.

DTOs puramente estruturais, entidades, enums, repositories declarativos, mappers gerados e classes de configuração sem regra de negócio podem ser candidatos a exclusão. Classes que implementam decisões, branches ou orquestração não devem ser excluídas apenas por serem difíceis de testar.

Os excludes devem ser avaliados individualmente em cada projeto antes de serem mantidos ou removidos.

## Validação antes da entrega

Execute a suíte completa do projeto:

```bash
mvnw test
```
