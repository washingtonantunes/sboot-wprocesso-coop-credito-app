package br.com.wti.creditorural.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.wti.TestBase;
import br.com.wti.core.exception.BusinessException;
import br.com.wti.integracao.cadastro.CadastroClient;
import br.com.wti.integracao.cadastro.dto.response.CooperadoApiResponseDTO;
import br.com.wti.integracao.cadastro.dto.response.CooperativaApiResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("OperacaoCreditoRuralControllerTest - contratação e regras de estado")
public class OperacaoCreditoRuralControllerTest extends TestBase {

    private static final String OPERACOES_URL = "/operacoes-credito-rural";

    @MockitoBean
    private CadastroClient cadastroClient;

    @Test
    @DisplayName("Deve contratar para cooperado ativo validado no cadastro")
    void deveContratarOperacao() throws Exception {
        mockCooperativa(1L);
        mockCadastroAtivo();
        String payload = carregarPayload("/payload/operacao-credito-rural/contratar.json");

        this.mockMvc
                .perform(post(OPERACOES_URL + "/contratar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cooperativaId").value(1))
                .andExpect(jsonPath("$.cooperadoId").value(10))
                .andExpect(jsonPath("$.cooperativaNome").value("Cooperativa 1"))
                .andExpect(jsonPath("$.cooperadoNome").value("Ana Souza"))
                .andExpect(jsonPath("$.produtoCodigo").value("CREDITO_RURAL"))
                .andExpect(jsonPath("$.status").value("SOLICITADA"));
    }

    @Test
    @DisplayName("Deve exigir valor positivo e finalidade na contratação")
    void deveExigirValorPositivoEFinalidadeNaContratacao() throws Exception {
        mockCooperativa(1L);

        String payload = carregarPayload("/payload/operacao-credito-rural/contratar.json")
                .replace("12345.67", "0")
                .replace("Custeio de produção agrícola", " ");

        this.mockMvc
                .perform(post(OPERACOES_URL + "/contratar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve exigir motivo ao negar e tipo, valor e motivo ao desclassificar")
    void deveExigirDadosDeNegativaEDesclassificacao() throws Exception {
        mockCentral();

        this.mockMvc
                .perform(put(OPERACOES_URL + "/100/negar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        this.mockMvc
                .perform(put(OPERACOES_URL + "/101/desclassificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve recusar contratação quando o cadastro rejeitar cooperado de outra cooperativa")
    void deveRecusarCooperadoDeOutraCooperativa() throws Exception {
        mockCooperativa(1L);
        when(this.cadastroClient.buscarCooperativa(1L)).thenReturn(cooperativaAtiva());
        when(this.cadastroClient.buscarCooperado(1L, 20L)).thenThrow(new BusinessException("Cooperado não encontrado"));
        String payload = carregarPayload("/payload/operacao-credito-rural/contratar.json")
                .replace("\"cooperadoId\": 10", "\"cooperadoId\": 20");

        this.mockMvc
                .perform(post(OPERACOES_URL + "/contratar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagemUsuario").value("Cooperado não encontrado"));

        this.mockMvc
                .perform(get(OPERACOES_URL + "/paginado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Deve recusar contratação quando o cadastro rejeitar cooperado inativo")
    void deveRecusarCooperadoInativo() throws Exception {
        mockCooperativa(1L);
        when(this.cadastroClient.buscarCooperativa(1L)).thenReturn(cooperativaAtiva());
        when(this.cadastroClient.buscarCooperado(1L, 12L)).thenThrow(new BusinessException("Cooperado não encontrado"));
        String payload = carregarPayload("/payload/operacao-credito-rural/contratar.json")
                .replace("\"cooperadoId\": 10", "\"cooperadoId\": 12");

        this.mockMvc
                .perform(post(OPERACOES_URL + "/contratar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagemUsuario").value("Cooperado não encontrado"));
    }

    @Test
    @DisplayName("Deve aprovar somente operação solicitada")
    void deveAprovarOperacaoSolicitada() throws Exception {
        mockCentral();

        this.mockMvc.perform(put(OPERACOES_URL + "/100/aprovar")).andExpect(status().isOk());

        this.mockMvc
                .perform(get(OPERACOES_URL + "/100/buscar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));
    }

    @Test
    @DisplayName("Deve negar operação solicitada e registrar motivo")
    void deveNegarOperacaoSolicitada() throws Exception {
        mockCentral();
        String payload = carregarPayload("/payload/operacao-credito-rural/negar.json");

        this.mockMvc
                .perform(put(OPERACOES_URL + "/100/negar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        this.mockMvc
                .perform(get(OPERACOES_URL + "/100/buscar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEGADA"))
                .andExpect(jsonPath("$.motivo").value("Documentação incompleta"));
    }

    @Test
    @DisplayName("Deve desclassificar totalmente operação aprovada pelo valor integral")
    void deveDesclassificarTotalmenteOperacaoAprovada() throws Exception {
        mockCentral();
        String payload = carregarPayload("/payload/operacao-credito-rural/desclassificar-total.json");

        this.mockMvc
                .perform(put(OPERACOES_URL + "/101/desclassificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        this.mockMvc
                .perform(get(OPERACOES_URL + "/101/buscar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DESCLASSIFICADA"))
                .andExpect(jsonPath("$.tipoDesclassificacao").value("TOTAL"))
                .andExpect(jsonPath("$.valorDesclassificado").value(50000.0));
    }

    @Test
    @DisplayName("Deve desclassificar parcialmente por valor inferior ao da operação")
    void deveDesclassificarParcialmenteOperacaoAprovada() throws Exception {
        mockCentral();
        String payload = carregarPayload("/payload/operacao-credito-rural/desclassificar-parcial.json");

        this.mockMvc
                .perform(put(OPERACOES_URL + "/101/desclassificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve rejeitar valor total de desclassificação diferente do valor da operação")
    void deveRejeitarValorTotalDiferenteDoValorDaOperacao() throws Exception {
        mockCentral();
        var mensagem = "Valor desclassificado incompatível com o tipo informado";
        String payload = carregarPayload("/payload/operacao-credito-rural/desclassificar-total-valor-invalido.json");

        this.mockMvc
                .perform(put(OPERACOES_URL + "/101/desclassificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Accept-Language", "pt-BR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagemUsuario").value(mensagem))
                .andExpect(jsonPath("$.mensagemDesenvolvedor").isNotEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar aprovação de operação que não está solicitada")
    void deveRejeitarAprovacaoDeOperacaoEmOutroEstado() throws Exception {
        mockCentral();

        this.mockMvc
                .perform(put(OPERACOES_URL + "/101/aprovar").header("Accept-Language", "pt-BR"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.mensagemUsuario").value("Operação não está no estado necessário para esta ação"));
    }

    private void mockCadastroAtivo() {
        CooperadoApiResponseDTO cooperado =
                carregarMock("/integracao/cadastro/cooperado-ativo.json", CooperadoApiResponseDTO.class);
        when(this.cadastroClient.buscarCooperativa(1L)).thenReturn(cooperativaAtiva());
        when(this.cadastroClient.buscarCooperado(1L, 10L)).thenReturn(cooperado);
    }

    private CooperativaApiResponseDTO cooperativaAtiva() {
        return carregarMock("/integracao/cadastro/cooperativa-ativa.json", CooperativaApiResponseDTO.class);
    }
}
