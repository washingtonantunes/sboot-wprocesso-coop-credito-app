package br.com.wti.core.tenant;

/**
 * Tipo do usuário, do perfil e da funcionalidade. Uma funcionalidade só entra em
 * um perfil, e um perfil só é atribuído a um usuário, quando os três têm o mesmo
 * tipo.
 */
public enum TipoPerfilEnum {
    CENTRAL,
    COOPERATIVA,
}
