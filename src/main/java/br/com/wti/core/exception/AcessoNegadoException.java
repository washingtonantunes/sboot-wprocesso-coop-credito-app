package br.com.wti.core.exception;

import java.io.Serial;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class AcessoNegadoException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    @Setter
    private static MessageSource messageSource;

    public AcessoNegadoException(String message) {
        super(message);
    }

    public static AcessoNegadoException createWith(String chave) {
        return new AcessoNegadoException(resolverMensagem(chave));
    }

    private static String resolverMensagem(String chave) {
        try {
            return messageSource.getMessage(chave, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar mensagem de acesso negado", e);
        }
    }
}
