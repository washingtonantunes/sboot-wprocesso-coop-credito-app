package br.com.wti.core.exception;

import java.io.Serial;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class IntegracaoException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private String mensagemUsuario;
    private String mensagemDesenvolvedor;

    public IntegracaoException(String message) {
        super(message);
    }

    public IntegracaoException(String mensagemUsuario, String mensagemDesenvolvedor) {
        super(mensagemDesenvolvedor);
        this.mensagemUsuario = mensagemUsuario;
        this.mensagemDesenvolvedor = mensagemDesenvolvedor;
    }

    public IntegracaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegracaoException(String message, String detail, Throwable cause) {
        super(message + (detail != null ? ": " + detail : ""), cause);
    }
}
