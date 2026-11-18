package br.com.wti.core.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@NoArgsConstructor
public class BusinessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    @Setter
    private static MessageSource messageSource;

    public BusinessException(String mensagem) {
        super(mensagem);
    }

    public static BusinessException createWith(String chave, Object... args) {
        return new BusinessException(resolverMensagem(chave, args));
    }

    private static String resolverMensagem(String chave, Object... args) {
        try {
            return messageSource.getMessage(chave, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar mensagem de regra de negocio", e);
        }
    }
}
