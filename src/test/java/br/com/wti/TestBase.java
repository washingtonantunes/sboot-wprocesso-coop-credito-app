package br.com.wti;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.wti.core.security.AutorizacaoInterceptor;
import br.com.wti.core.security.JwtService;
import br.com.wti.core.security.JwtServicoInternoService;
import br.com.wti.core.security.SecurityService;
import br.com.wti.core.security.ServiceTokenProvider;
import br.com.wti.core.security.UsuarioAutenticadoDTO;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.Jackson3JsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.Jackson3MappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SqlGroup({
    @Sql(value = "/queries/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/queries/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public class TestBase {

    @BeforeAll
    static void configurarJsonPath() {
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new Jackson3JsonProvider();
            private final MappingProvider mappingProvider = new Jackson3MappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected JwtServicoInternoService jwtServicoInternoService;

    @Autowired
    protected ServiceTokenProvider serviceTokenProvider;

    @MockitoBean
    protected AutorizacaoInterceptor autorizacaoInterceptor;

    @MockitoBean
    protected SecurityService securityService;

    @BeforeEach
    void setup() {
        when(this.autorizacaoInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    // A autorização por funcionalidade fica mockada (interceptor sempre libera); o
    // que estes métodos definem é o tipo e a cooperativa do usuário da requisição.

    protected void mockCentral() {
        mockUsuario(1L, "Central Teste", "CENTRAL", null);
    }

    protected void mockCooperativa(Long cooperativaId) {
        mockUsuario(100L, "Cooperativa Teste", "COOPERATIVA", cooperativaId);
    }

    protected void mockUsuario(Long id, String nome, String tipo, Long cooperativaId) {
        UsuarioAutenticadoDTO usuario = new UsuarioAutenticadoDTO(id, nome, tipo, cooperativaId, List.of(), Map.of());
        when(this.securityService.getUsuarioAutenticado()).thenReturn(usuario);
    }

    // Tokens reais para os cenários que exercem a autorização verdadeira (ver usarAutorizacaoReal
    // em cada classe de teste). As funcionalidades do cenário entram por parâmetro.

    protected String tokenCentral(List<String> funcionalidades) {
        return "Bearer " + this.jwtService.gerarToken(1L, "Central Teste", "CENTRAL", null, funcionalidades, Map.of());
    }

    protected String tokenCooperativa(Long cooperativaId, List<String> funcionalidades) {
        return "Bearer "
                + this.jwtService.gerarToken(
                        100L, "Cooperativa Teste", "COOPERATIVA", cooperativaId, funcionalidades, Map.of());
    }

    protected String tokenServico() {
        return "Bearer " + this.serviceTokenProvider.getToken();
    }

    protected String carregarPayload(String caminho) {
        try {
            Resource file = new ClassPathResource(caminho);
            return Files.readString(Paths.get(file.getURI()));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao carregar payload: " + caminho, e);
        }
    }

    protected <T> T carregarMock(String caminho, Class<T> tipo) {
        try {
            Resource file = new ClassPathResource(caminho);
            return this.objectMapper.readValue(file.getInputStream(), tipo);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao carregar mock: " + caminho, e);
        }
    }
}
