package br.com.wti.core.auditoria;

public class AuditoriaContextHolder {

    private static final ThreadLocal<String> USUARIO = new ThreadLocal<>();

    private AuditoriaContextHolder() {}

    public static void setUsuario(String usuario) {
        USUARIO.set(usuario);
    }

    public static String getUsuario() {
        String usuario = USUARIO.get();
        return usuario != null ? usuario : "Sistema";
    }

    public static void clear() {
        USUARIO.remove();
    }
}
