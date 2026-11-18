package br.com.wti.creditorural.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.wti.TestBase;
import br.com.wti.core.security.AutorizacaoInterceptor;
import br.com.wti.core.security.JwtService;
import br.com.wti.core.security.JwtServicoInternoService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

@DisplayName("OperacaoCreditoRuralAutorizacaoControllerTest - autorização por funcionalidade")
class OperacaoCreditoRuralAutorizacaoControllerTest extends TestBase {

    private static final String URL = "/operacoes-credito-rural";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtServicoInternoService jwtServicoInternoService;

    @BeforeEach
    void usarAutorizacaoReal() {
        AutorizacaoInterceptor interceptorReal =
                new AutorizacaoInterceptor(jwtService, jwtServicoInternoService, new AntPathMatcher());
        when(this.autorizacaoInterceptor.preHandle(any(), any(), any()))
                .thenAnswer(invocation -> interceptorReal.preHandle(
                        invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2)));
        when(this.securityService.getUsuarioAutenticado()).thenCallRealMethod();
    }

    @Test
    @DisplayName("Cada ação exige a funcionalidade correspondente")
    void cadaAcaoExigeFuncionalidadeCorrespondente() throws Exception {
        negar(get(URL + "/paginado"), "COOPERATIVA", 1L, "OUTRA");
        negar(get(URL + "/paginado"), "CENTRAL", null, "OUTRA");
        negar(get(URL + "/100/buscar"), "COOPERATIVA", 1L, "OUTRA");
        negar(get(URL + "/100/buscar"), "CENTRAL", null, "OUTRA");
        negar(
                post(URL + "/contratar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carregarPayload("/payload/operacao-credito-rural/contratar.json")),
                "COOPERATIVA",
                1L,
                "OUTRA");
        negar(put(URL + "/100/aprovar"), "CENTRAL", null, "OUTRA");
        negar(
                put(URL + "/100/negar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carregarPayload("/payload/operacao-credito-rural/negar.json")),
                "CENTRAL",
                null,
                "OUTRA");
        negar(
                put(URL + "/101/desclassificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carregarPayload("/payload/operacao-credito-rural/desclassificar-total.json")),
                "CENTRAL",
                null,
                "OUTRA");
    }

    @Test
    @DisplayName("Ação exclusiva da Central é negada para usuário de cooperativa")
    void acaoDaCentralEExclusiva() throws Exception {
        this.mockMvc
                .perform(put(URL + "/100/aprovar")
                        .header(HttpHeaders.AUTHORIZATION, token("COOPERATIVA", 1L, "CREDITO_RURAL_CONTRATAR")))
                .andExpect(status().isForbidden());
    }

    private void negar(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
            String tipo,
            Long cooperativaId,
            String funcionalidade)
            throws Exception {
        this.mockMvc
                .perform(request.header(HttpHeaders.AUTHORIZATION, token(tipo, cooperativaId, funcionalidade)))
                .andExpect(status().isForbidden());
    }

    private String token(String tipo, Long cooperativaId, String funcionalidade) {
        Long usuarioId = cooperativaId == null ? 1L : 100L;
        return "Bearer "
                + jwtService.gerarToken(usuarioId, "Teste", tipo, cooperativaId, List.of(funcionalidade), Map.of());
    }
}
