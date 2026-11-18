package br.com.wti.core.tenant;

import org.springframework.data.jpa.domain.Specification;

public class EscopoSpecification {

    private static final String ATRIBUTO_COOPERATIVA = "cooperativaId";
    private static final String ATRIBUTO_TIPO = "tipo";

    private EscopoSpecification() {}

    /**
     * Restringe a consulta ao tipo recebido. Vale para qualquer entidade global que
     * tenha o atributo {@code tipo} e exista separada por tipo de usuário (como a
     * funcionalidade), devolvendo só os registros do tipo do usuário autenticado.
     */
    public static <T> Specification<T> doTipo(TipoPerfilEnum tipo) {
        return (root, query, builder) -> builder.equal(root.get(ATRIBUTO_TIPO), tipo);
    }

    /**
     * Restringe a consulta ao escopo recebido. Vale para qualquer entidade que tenha
     * o atributo {@code cooperativaId}; deve ser combinada com a specification de
     * filtro da entidade em toda listagem de dado que pertence a uma cooperativa.
     */
    public static <T> Specification<T> noEscopo(EscopoCooperativa escopo) {
        return (root, query, builder) -> {
            if (escopo.isTodas()) {
                return builder.conjunction();
            }
            return builder.equal(root.get(ATRIBUTO_COOPERATIVA), escopo.cooperativaId());
        };
    }
}
