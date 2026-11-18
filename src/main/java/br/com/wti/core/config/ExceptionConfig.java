package br.com.wti.core.config;

import br.com.wti.core.exception.AcessoNegadoException;
import br.com.wti.core.exception.BusinessException;
import br.com.wti.core.exception.EntidadeNaoEncontradaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExceptionConfig {

    @Autowired
    public void configureBusinessException(MessageSource messageSource) {
        BusinessException.setMessageSource(messageSource);
    }

    @Autowired
    public void configureEntidadeNaoEncontradaException(MessageSource messageSource) {
        EntidadeNaoEncontradaException.setMessageSource(messageSource);
    }

    @Autowired
    public void configureAcessoNegadoException(MessageSource messageSource) {
        AcessoNegadoException.setMessageSource(messageSource);
    }
}
