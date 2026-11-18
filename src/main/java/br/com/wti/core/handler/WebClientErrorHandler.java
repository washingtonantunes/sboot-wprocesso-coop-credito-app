package br.com.wti.core.handler;

import br.com.wti.core.exception.BusinessException;
import br.com.wti.core.exception.IntegracaoException;
import br.com.wti.core.security.ServiceTokenProvider;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
public class WebClientErrorHandler {

    private static final long MAX_TENTATIVAS_CONEXAO = 3;

    private final WebClient webClient;
    private final ServiceTokenProvider tokenProvider;

    public WebClientErrorHandler(WebClient webClient, ServiceTokenProvider tokenProvider) {
        this.webClient = webClient;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Método genérico que executa uma chamada HTTP e aplica a resposta ao tipo apropriado
     */
    private <T, R> Mono<R> executeCallInternal(
            HttpMethod httpMethod,
            String uri,
            T requestBody,
            String errorMessagePrefix,
            java.util.function.Function<WebClient.ResponseSpec, Mono<R>> responseMapper) {

        WebClient.RequestBodyUriSpec requestBodyUriSpec = webClient.method(httpMethod);
        WebClient.RequestHeadersSpec<?> requestSpec;

        if (requestBody != null && (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT)) {
            requestSpec = requestBodyUriSpec
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getToken())
                    .bodyValue(requestBody);
        } else {
            requestSpec =
                    requestBodyUriSpec.uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getToken());
        }

        WebClient.ResponseSpec responseSpec = requestSpec
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            try {
                                JsonMapper mapper = JsonMapper.builder().build();
                                JsonNode jsonBody = mapper.readTree(body);

                                String mensagemUsuarioExterna =
                                        jsonBody.path("mensagemUsuario").asString();
                                String mensagemDesenvolvedorExterna =
                                        jsonBody.path("mensagemDesenvolvedor").asString();
                                String tipoErro = jsonBody.path("tipoErro").asString(null);

                                if ("NEGOCIO".equals(tipoErro)) {
                                    return Mono.error(new BusinessException(mensagemUsuarioExterna));
                                }

                                String mensagemUsuario =
                                        mensagemUsuarioExterna == null || mensagemUsuarioExterna.isBlank()
                                                ? "Falha ao se comunicar com API externa."
                                                : mensagemUsuarioExterna;
                                String mensagemDesenvolvedor =
                                        "IntegracaoException: " + errorMessagePrefix + " - Erro da API ("
                                                + response.statusCode().value() + "): " + mensagemDesenvolvedorExterna;

                                return Mono.error(new IntegracaoException(mensagemUsuario, mensagemDesenvolvedor));

                            } catch (Exception e) {
                                String mensagemDesenvolvedor = errorMessagePrefix + " - Erro da API ("
                                        + response.statusCode().value() + "): " + body;
                                return Mono.error(new IntegracaoException(
                                        "Falha ao se comunicar com API externa.", mensagemDesenvolvedor));
                            }
                        }));

        return responseMapper
                .apply(responseSpec)
                .doOnError(
                        this::isFalhaTemporariaDeConexao,
                        ex -> log.warn("Falha temporária ao chamar {}: {}", uri, ex.getMessage()))
                .retryWhen(Retry.backoff(MAX_TENTATIVAS_CONEXAO - 1, Duration.ofSeconds(2))
                        .filter(this::isFalhaTemporariaDeConexao)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            String finalErrorMessage = errorMessagePrefix + " - Falha ao conectar/comunicar com a API "
                                    + uri + " após " + (retrySignal.totalRetries() + 1) + " tentativas.";
                            return new IntegracaoException(finalErrorMessage, retrySignal.failure());
                        }))
                .onErrorMap(Exception.class, ex -> {
                    if (ex instanceof IntegracaoException || ex instanceof BusinessException) {
                        return ex;
                    }
                    String unexpectedErrorMessage =
                            errorMessagePrefix + " - Erro inesperado na integração com URI " + uri;
                    return new IntegracaoException(unexpectedErrorMessage, ex.getMessage(), ex);
                });
    }

    private boolean isFalhaTemporariaDeConexao(Throwable erro) {
        Throwable causa = erro;

        while (causa != null) {
            if (causa instanceof ConnectException || causa instanceof SocketTimeoutException) {
                return true;
            }

            causa = causa.getCause();
        }

        return false;
    }

    /**
     * Executa uma chamada HTTP genérica com Class<R>
     */
    public <T, R> Mono<R> executeCall(
            HttpMethod httpMethod, String uri, T requestBody, Class<R> responseType, String errorMessagePrefix) {

        return executeCallInternal(
                httpMethod,
                uri,
                requestBody,
                errorMessagePrefix,
                responseSpec -> responseSpec.bodyToMono(responseType));
    }

    /**
     * Executa uma chamada HTTP genérica com ParameterizedTypeReference<R>
     */
    public <T, R> Mono<R> executeCall(
            HttpMethod httpMethod,
            String uri,
            T requestBody,
            ParameterizedTypeReference<R> responseType,
            String errorMessagePrefix) {

        return executeCallInternal(
                httpMethod,
                uri,
                requestBody,
                errorMessagePrefix,
                responseSpec -> responseSpec.bodyToMono(responseType));
    }

    // --- Métodos de Conveniência ---

    public <T, R> Mono<R> post(String uri, T requestBody, Class<R> responseType, String errorMessagePrefix) {
        return executeCall(HttpMethod.POST, uri, requestBody, responseType, errorMessagePrefix);
    }

    public <T, R> Mono<R> post(
            String uri, T requestBody, ParameterizedTypeReference<R> responseType, String errorMessagePrefix) {
        return executeCall(HttpMethod.POST, uri, requestBody, responseType, errorMessagePrefix);
    }

    public <R> Mono<R> get(String uri, Class<R> responseType, String errorMessagePrefix) {
        return executeCall(HttpMethod.GET, uri, null, responseType, errorMessagePrefix);
    }

    public <R> Mono<R> get(String uri, ParameterizedTypeReference<R> responseType, String errorMessagePrefix) {
        return executeCall(HttpMethod.GET, uri, null, responseType, errorMessagePrefix);
    }

    public <T, R> Mono<R> put(String uri, T requestBody, Class<R> responseType, String errorMessagePrefix) {
        return executeCall(HttpMethod.PUT, uri, requestBody, responseType, errorMessagePrefix);
    }

    public <T, R> Mono<R> put(
            String uri, T requestBody, ParameterizedTypeReference<R> responseType, String errorMessagePrefix) {
        return executeCall(HttpMethod.PUT, uri, requestBody, responseType, errorMessagePrefix);
    }

    public <R> Mono<R> delete(String uri, Class<R> responseType, String errorMessagePrefix) {
        return executeCall(HttpMethod.DELETE, uri, null, responseType, errorMessagePrefix);
    }

    public <R> Mono<R> delete(String uri, ParameterizedTypeReference<R> responseType, String errorMessagePrefix) {
        return executeCall(HttpMethod.DELETE, uri, null, responseType, errorMessagePrefix);
    }

    public Mono<Void> delete(String uri, String errorMessagePrefix) {
        return executeCall(HttpMethod.DELETE, uri, null, Void.class, errorMessagePrefix);
    }
}
