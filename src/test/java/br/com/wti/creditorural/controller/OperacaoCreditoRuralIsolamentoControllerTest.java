package br.com.wti.creditorural.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.wti.TestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OperacaoCreditoRuralIsolamentoControllerTest - isolamento por cooperativa")
class OperacaoCreditoRuralIsolamentoControllerTest extends TestBase {

    private static final String URL = "/operacoes-credito-rural";

    @Test
    @DisplayName("Cooperativa lista somente as próprias operações")
    void cooperativaListaSomenteAsPropriasOperacoes() throws Exception {
        mockCooperativa(1L);

        this.mockMvc
                .perform(get(URL + "/paginado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].cooperativaId", everyItem(equalTo(1))));
    }

    @Test
    @DisplayName("Cooperativa recebe não encontrado ao buscar operação de outra cooperativa")
    void cooperativaNaoBuscaOperacaoDeOutraCooperativa() throws Exception {
        mockCooperativa(1L);
        var mensagem = "Operação de crédito rural não encontrada";

        this.mockMvc
                .perform(get(URL + "/200/buscar").header("Accept-Language", "pt-BR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagemUsuario").value(mensagem))
                .andExpect(jsonPath("$.mensagemDesenvolvedor").isNotEmpty());
    }

    @Test
    @DisplayName("Cooperativa informada na requisição é ignorada em favor do escopo")
    void cooperativaInformadaEIgnorada() throws Exception {
        mockCooperativa(1L);

        this.mockMvc
                .perform(get(URL + "/paginado").param("cooperativaId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].cooperativaId", everyItem(equalTo(1))));
    }

    @Test
    @DisplayName("Central lista operações de todas as cooperativas e filtra por cooperativa")
    void centralListaTodasOuFiltraPorCooperativa() throws Exception {
        mockCentral();

        this.mockMvc
                .perform(get(URL + "/paginado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        this.mockMvc
                .perform(get(URL + "/paginado").param("cooperativaNome", "Cooperativa 2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].cooperativaId").value(2))
                .andExpect(jsonPath("$.content[0].cooperativaNome").value("Cooperativa 2"));
    }
}
