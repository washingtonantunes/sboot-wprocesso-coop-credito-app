package br.com.wti.core.tenant;

/**
 * Relações entre o grupo (front) e o tipo de perfil, reunidas em um único ponto para
 * que a entrada de um tipo novo exija mudar apenas aqui. O escopo de dados de cada
 * tipo, terceira relação, fica em {@link TenantContext}.
 */
public final class RelacoesTipo {

    private RelacoesTipo() {}

    /**
     * Tipos de usuário que cada grupo aceita no login. Um grupo sem tipo fixo (como
     * {@code DOCS}) aceita qualquer tipo.
     */
    public static boolean grupoAceitaTipo(GrupoEnum grupo, TipoPerfilEnum tipo) {
        return switch (grupo) {
            case CENTRAL -> tipo == TipoPerfilEnum.CENTRAL;
            case COOPERATIVA -> tipo == TipoPerfilEnum.COOPERATIVA;
            case DOCS -> true;
        };
    }

    /**
     * Tipos que um tipo de usuário pode gerenciar (perfis, funcionalidades e usuários).
     * Hoje cada tipo gerencia apenas o próprio tipo.
     */
    public static boolean podeGerenciar(TipoPerfilEnum executor, TipoPerfilEnum alvo) {
        return executor == alvo;
    }
}
