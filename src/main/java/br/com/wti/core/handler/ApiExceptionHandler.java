package br.com.wti.core.handler;

import br.com.wti.core.exception.AcessoNegadoException;
import br.com.wti.core.exception.BusinessException;
import br.com.wti.core.exception.EntidadeNaoEncontradaException;
import br.com.wti.core.exception.IntegracaoException;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.MismatchedInputException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @Autowired
    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String mensagemUsuario;
        String mensagemDesenvolvedor =
                Optional.ofNullable(ex.getCause()).orElse(ex).toString();

        Throwable causa = ex.getCause();

        if (causa instanceof MismatchedInputException mismatch) {
            String campo = extrairCampoDoPath(mismatch);
            String tipoEsperado =
                    mismatch.getTargetType() != null ? mismatch.getTargetType().getSimpleName() : "desconhecido";

            if (campo != null) {
                mensagemUsuario = messageSource.getMessage(
                        "erro.formato-invalido.campo",
                        new Object[] {campo, tipoEsperado},
                        LocaleContextHolder.getLocale());
            } else {
                mensagemUsuario = messageSource.getMessage(
                        "erro.formato-invalido", new Object[] {tipoEsperado}, LocaleContextHolder.getLocale());
            }
        } else {
            mensagemUsuario = messageSource.getMessage("mensagem.invalida", null, LocaleContextHolder.getLocale());
        }

        Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor);
        return handleExceptionInternal(ex, erro, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Erro erros = criarErroConcatenado(ex.getBindingResult());
        return handleExceptionInternal(ex, erros, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String mensagemUsuario =
                this.messageSource.getMessage("recurso.nao-encontrado", null, LocaleContextHolder.getLocale());
        String mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
        Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor);
        return handleExceptionInternal(ex, erro, headers, HttpStatus.NOT_FOUND, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String nomeParametro = ex.getParameterName();
        String tipoParametro = ex.getParameterType();

        String mensagemUsuario = messageSource.getMessage(
                "erro.parametro.obrigatorio",
                new Object[] {nomeParametro, tipoParametro},
                LocaleContextHolder.getLocale());

        String mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
        Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor);
        return handleExceptionInternal(ex, erro, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        String mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
        String nomeUk = extrairNomeUk(mensagemDesenvolvedor);
        String mensagemUsuario = resolverMensagemUk(nomeUk);

        Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor);
        return handleExceptionInternal(ex, erro, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(final BusinessException ex, WebRequest request) {
        String mensagemUsuario = ex.getMessage();
        String mensagemDesenvolvedor = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor, "NEGOCIO");
        return handleExceptionInternal(ex, erro, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<Object> handleEntidadeNaoEncontradaException(
            final EntidadeNaoEncontradaException ex, WebRequest request) {
        String mensagemUsuario = ex.getMessage();
        String mensagemDesenvolvedor = ex.getClass().getSimpleName() + ": " + mensagemUsuario;
        Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor);
        return handleExceptionInternal(ex, erro, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(IntegracaoException.class)
    public ResponseEntity<Object> handleIntegracaoException(final IntegracaoException ex, WebRequest request) {
        String mensagemUsuario;
        String mensagemDesenvolvedor;

        if (ex.getMensagemUsuario() != null && ex.getMensagemDesenvolvedor() != null) {
            mensagemUsuario = ex.getMensagemUsuario();
            mensagemDesenvolvedor = ex.getMensagemDesenvolvedor();
        } else {
            mensagemUsuario = ex.getMessage();
            mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
        }

        Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor);
        return handleExceptionInternal(ex, erro, new HttpHeaders(), HttpStatus.BAD_GATEWAY, request);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Object> handleAcessoNegadoException(final Exception ex, WebRequest request) {
        String mensagemUsuario = ex.getMessage();
        String mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
        Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor);
        return handleExceptionInternal(ex, erro, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleNullPointerException(final NullPointerException ex, WebRequest request) {
        Erro erro = new Erro(
                "Ocorreu um erro interno ao processar a solicitação.",
                "Falha inesperada. Consulte os logs do servidor.");
        return handleExceptionInternal(ex, erro, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(final Exception ex, WebRequest request) {
        String mensagemUsuario = ex.getMessage();
        String mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
        Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor);
        return handleExceptionInternal(ex, erro, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private static final Pattern PATTERN_NOME_UK = Pattern.compile("UK_[A-Z0-9_]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_SUFIXO_INDEX_H2 =
            Pattern.compile("_INDEX_[A-Z0-9]+$", Pattern.CASE_INSENSITIVE);

    /**
     * Extrai o nome da UNIQUE KEY da mensagem de erro, independente do dialeto do banco (MySQL, H2,
     * PostgreSQL, Oracle, …). Depende apenas do nome da constraint, igual em todos os bancos
     * porque é definido no schema. Remove o sufixo {@code _INDEX_<X>} que o H2 anexa ao nome do
     * índice interno gerado para a UK.
     *
     * <p>Convenção obrigatória do schema: nomes de UK começam com {@code UK_}. Ver
     * {@code docs/SCRIPTS_SQL.md}. Cada UK tem uma chave {@code uk.<NOME>} no
     * {@code uk-messages.properties} com a mensagem de negócio correspondente.
     */
    private String extrairNomeUk(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        Matcher matcher = PATTERN_NOME_UK.matcher(errorMessage);
        if (!matcher.find()) {
            return null;
        }
        String nome = matcher.group().toUpperCase();
        return PATTERN_SUFIXO_INDEX_H2.matcher(nome).replaceFirst("");
    }

    private String resolverMensagemUk(String nomeUk) {
        if (nomeUk != null) {
            try {
                return this.messageSource.getMessage("uk." + nomeUk, null, LocaleContextHolder.getLocale());
            } catch (org.springframework.context.NoSuchMessageException ignored) {
                // Sem mensagem específica → cai no fallback genérico.
            }
        }
        return this.messageSource.getMessage("recurso.operacao-nao-permitida", null, LocaleContextHolder.getLocale());
    }

    private String extrairCampoDoPath(MismatchedInputException ex) {
        if (ex.getPath() == null || ex.getPath().isEmpty()) {
            return null;
        }

        return ex.getPath().stream()
                .map(ref -> ref.getPropertyName() != null ? ref.getPropertyName() : "[" + ref.getIndex() + "]")
                .collect(java.util.stream.Collectors.joining("."));
    }

    private Erro criarErroConcatenado(BindingResult bindingResult) {
        StringBuilder mensagemUsuarioBuilder = new StringBuilder();
        StringBuilder mensagemDesenvolvedorBuilder = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String mensagemUsuario = fieldError.getField() + " - "
                    + this.messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
            String mensagemDesenvolvedor = fieldError.toString();

            mensagemUsuarioBuilder.append(mensagemUsuario).append("; ");
            mensagemDesenvolvedorBuilder.append(mensagemDesenvolvedor).append("; ");
        }

        for (ObjectError globalError : bindingResult.getGlobalErrors()) {
            String mensagemUsuario = this.messageSource.getMessage(globalError, LocaleContextHolder.getLocale());
            String mensagemDesenvolvedor = globalError.toString();

            mensagemUsuarioBuilder.append(mensagemUsuario).append("; ");
            mensagemDesenvolvedorBuilder.append(mensagemDesenvolvedor).append("; ");
        }

        String mensagemUsuarioConcatenada = mensagemUsuarioBuilder.toString().trim();
        String mensagemDesenvolvedorConcatenada =
                mensagemDesenvolvedorBuilder.toString().trim();

        if (mensagemUsuarioConcatenada.endsWith(";")) {
            mensagemUsuarioConcatenada =
                    mensagemUsuarioConcatenada.substring(0, mensagemUsuarioConcatenada.length() - 1);
        }
        if (mensagemDesenvolvedorConcatenada.endsWith(";")) {
            mensagemDesenvolvedorConcatenada =
                    mensagemDesenvolvedorConcatenada.substring(0, mensagemDesenvolvedorConcatenada.length() - 1);
        }

        return new Erro(mensagemUsuarioConcatenada, mensagemDesenvolvedorConcatenada);
    }

    @Schema(name = "Erro", description = "Model que sera devolvido por todo e qualquer tipo de erro do backend")
    @Getter
    public static class Erro {

        @Schema(example = "Mensagem Usuario")
        private final String mensagemUsuario;

        @Schema(example = "Mensagem Desenvolvedor")
        private final String mensagemDesenvolvedor;

        private final String tipoErro;

        public Erro(String mensagemUsuario, String mensagemDesenvolvedor) {
            this.mensagemUsuario = mensagemUsuario;
            this.mensagemDesenvolvedor = mensagemDesenvolvedor;
            this.tipoErro = null;
        }

        public Erro(String mensagemUsuario, String mensagemDesenvolvedor, String tipoErro) {
            this.mensagemUsuario = mensagemUsuario;
            this.mensagemDesenvolvedor = mensagemDesenvolvedor;
            this.tipoErro = tipoErro;
        }
    }
}
