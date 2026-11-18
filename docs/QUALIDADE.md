# Qualidade e formatação de código

## Objetivo

O projeto usa o Spotless com `palantir-java-format` como fonte única de verdade para a formatação dos arquivos Java. A mesma regra é aplicada pelo Maven e pelo IntelliJ, impedindo diferenças causadas por configurações individuais da IDE.

O formatador usa espaços, indentação de quatro colunas e largura máxima de 140 caracteres. Ele organiza imports, remove espaços em branco no fim das linhas e garante uma quebra de linha ao término de cada arquivo Java. Comentários Javadoc não são reformatados automaticamente.

## Comandos Maven

Antes de enviar uma alteração, aplique a formatação:

```bash
mvnw spotless:apply
```

Para apenas verificar, sem modificar arquivos:

```bash
mvnw spotless:check
```

O `spotless:check` é executado na fase `validate`. Portanto, os comandos usuais do ciclo Maven, incluindo `mvnw test` e `mvnw package`, falham quando houver arquivos Java fora do padrão. A correção continua sendo explícita com `mvnw spotless:apply`, da mesma forma que `format` e `format:check` nos frontends.

## Regras estáticas

O arquivo `config/checkstyle/checkstyle.xml` reúne as regras estáticas verificadas pelo Checkstyle. Ele é o equivalente ao arquivo de regras do ESLint nos frontends e também é executado na fase `validate`.

O Checkstyle exige nomes Java convencionais, ordem canônica dos modificadores, chaves em estruturas de controle, uma instrução por linha e imports sem curinga. Também proíbe blocos e statements vazios, `fall-through` acidental em `switch`, saída em console e `printStackTrace`.

Para executar apenas essa validação, sem alterar arquivos:

```bash
mvnw checkstyle:check
```

## IntelliJ IDEA

1. Em `Settings > Plugins > Marketplace`, instale `palantir-java-format`.
2. Em `Settings > palantir-java-format Settings`, habilite `Enable palantir-java-format` para este projeto.
3. Use `Code > Reformat Code` ou `Ctrl+Alt+L` para formatar o arquivo aberto ou a seleção.

Com o plugin habilitado, a ação padrão de reformatação do IntelliJ usa o mesmo formatador configurado pelo Maven. O build permanece a validação obrigatória do repositório.
