package br.com.wti.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Value("${server.servlet.context-path}")
    private String contexto;

    @Bean
    OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl(contexto);

        return new OpenAPI()
                .info(new Info()
                        .title("WProcesso Coop Credito API")
                        .version("v1")
                        .description("Sistema de Gestão de Credito."))
                .servers(List.of(server))
                .components(new Components());
    }
}
