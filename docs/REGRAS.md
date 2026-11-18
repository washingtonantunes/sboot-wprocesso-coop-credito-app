# Regras do projeto

- A cooperativa de uma operação vem sempre do `EscopoCooperativa` resolvido pelo core.
- A contratação só grava após validar o identificador do cooperado ativo pelo canal interno do cadastro.
- A contratação exige valor positivo com até duas casas decimais e finalidade.
- Os identificadores externos e nomes de cooperativa e cooperado são mantidos em tabelas locais normalizadas.
- Aprovação e negativa só alteram operações `SOLICITADA`.
- A negativa exige motivo.
- Desclassificação só altera operação `APROVADA`; o valor total deve ser igual ao valor da operação e o parcial deve ser menor.
- A desclassificação exige tipo, valor positivo com até duas casas decimais e motivo.
- A cooperativa consulta somente operações do próprio escopo; a Central pode consultar todas ou filtrar por cooperativa.
- A API aplica autorização por funcionalidade de ação de negócio.
