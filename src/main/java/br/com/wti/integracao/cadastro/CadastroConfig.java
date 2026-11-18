package br.com.wti.integracao.cadastro;

import br.com.wti.core.handler.WebClientErrorHandler;
import br.com.wti.core.security.ServiceTokenProvider;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class CadastroConfig {

    @Value("${app.cadastro.endpoint}")
    private String endpoint;

    @Value("${app.cadastro.timeout}")
    private Duration timeout;

    @Value("${app.cadastro.max-response-size}")
    private DataSize maxResponseSize;

    @Bean
    public WebClient webClientCadastro(JsonMapper jsonMapper) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize((int) this.maxResponseSize.toBytes());
                    configurer
                            .defaultCodecs()
                            .jacksonJsonEncoder(new JacksonJsonEncoder(jsonMapper, MediaType.APPLICATION_JSON));
                    configurer
                            .defaultCodecs()
                            .jacksonJsonDecoder(new JacksonJsonDecoder(jsonMapper, MediaType.APPLICATION_JSON));
                })
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(this.timeout).followRedirect(true)))
                .exchangeStrategies(strategies)
                .baseUrl(this.endpoint)
                .build();
    }

    @Bean
    public WebClientErrorHandler cadastroApiErrorHandler(
            WebClient webClientCadastro, ServiceTokenProvider serviceTokenProvider) {
        return new WebClientErrorHandler(webClientCadastro, serviceTokenProvider);
    }
}
