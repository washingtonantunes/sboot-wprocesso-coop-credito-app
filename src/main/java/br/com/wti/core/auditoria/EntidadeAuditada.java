package br.com.wti.core.auditoria;

import br.com.wti.core.tenant.EscopoCooperativa;
import java.util.function.Function;

/**
 * Entidade cujo histórico de alterações pode ser consultado, com o alcance que o
 * histórico tem em relação ao escopo de quem consulta. O domínio de cada back
 * declara as suas em um {@link CatalogoAuditoria}; a decisão de escopo fica aqui.
 */
public final class EntidadeAuditada {

    private enum Alcance {
        GLOBAL,
        SOMENTE_TODAS_AS_COOPERATIVAS,
        POR_COOPERATIVA
    }

    private final String nome;
    private final Class<?> classe;
    private final Alcance alcance;
    private final Function<Object, Long> cooperativaDoRegistro;

    private EntidadeAuditada(
            String nome, Class<?> classe, Alcance alcance, Function<Object, Long> cooperativaDoRegistro) {
        this.nome = nome;
        this.classe = classe;
        this.alcance = alcance;
        this.cooperativaDoRegistro = cooperativaDoRegistro;
    }

    /** Entidade global cujo histórico é visível em qualquer escopo. */
    public static EntidadeAuditada global(String nome, Class<?> classe) {
        return new EntidadeAuditada(nome, classe, Alcance.GLOBAL, null);
    }

    /** Entidade global cujo histórico só é visível a quem tem escopo de todas as cooperativas. */
    public static EntidadeAuditada somenteTodasAsCooperativas(String nome, Class<?> classe) {
        return new EntidadeAuditada(nome, classe, Alcance.SOMENTE_TODAS_AS_COOPERATIVAS, null);
    }

    /**
     * Entidade que pertence a uma cooperativa. O histórico só é visível quando a
     * cooperativa do registro está no escopo; registro sem cooperativa só aparece
     * para o escopo de todas.
     */
    public static <T> EntidadeAuditada porCooperativa(
            String nome, Class<T> classe, Function<T, Long> cooperativaDoRegistro) {
        return new EntidadeAuditada(
                nome, classe, Alcance.POR_COOPERATIVA, registro -> cooperativaDoRegistro.apply(classe.cast(registro)));
    }

    public String nome() {
        return nome;
    }

    public Class<?> classe() {
        return classe;
    }

    boolean pertenceACooperativa() {
        return alcance == Alcance.POR_COOPERATIVA;
    }

    /**
     * Indica se o histórico do registro pode ser devolvido no escopo recebido. O
     * registro só é consultado para entidade que pertence a uma cooperativa.
     */
    boolean visivelNoEscopo(EscopoCooperativa escopo, Object registro) {
        if (escopo.isTodas() || alcance == Alcance.GLOBAL) {
            return true;
        }
        if (alcance == Alcance.SOMENTE_TODAS_AS_COOPERATIVAS) {
            return false;
        }
        return registro != null && escopo.abrange(cooperativaDoRegistro.apply(registro));
    }
}
