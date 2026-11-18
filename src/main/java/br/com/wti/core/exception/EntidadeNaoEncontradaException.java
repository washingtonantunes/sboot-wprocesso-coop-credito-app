package br.com.wti.core.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@NoArgsConstructor
public class EntidadeNaoEncontradaException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    @Setter
    private static MessageSource messageSource;

    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }

    public static EntidadeNaoEncontradaException createWith(String chave) {
        return new EntidadeNaoEncontradaException(resolverMensagem(chave));
    }

    private static String resolverMensagem(String chave) {
        try {
            return messageSource.getMessage(chave, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar mensagem de entidade nao encontrada", e);
        }
    }
}
