package br.com.wti.core.tenant;

/**
 * Grupo da funcionalidade, equivalente ao front que a expõe. O login filtra as
 * funcionalidades do token pelo grupo do front que originou a requisição. A relação
 * entre grupo e tipo de usuário fica em {@code core/tenant}.
 */
public enum GrupoEnum {
    CENTRAL,
    COOPERATIVA,
    DOCS,
}
