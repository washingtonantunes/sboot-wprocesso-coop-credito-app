package br.com.wti.integracao.cadastro;

import br.com.wti.core.handler.WebClientErrorHandler;
import br.com.wti.integracao.cadastro.dto.response.CooperadoApiResponseDTO;
import br.com.wti.integracao.cadastro.dto.response.CooperativaApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CadastroClient {

    private static final String COOPERATIVA_PATH = "/api/cooperativas";

    private final WebClientErrorHandler cadastroApiErrorHandler;

    public CooperadoApiResponseDTO buscarCooperado(Long cooperativaId, Long cooperadoId) {
        String path = COOPERATIVA_PATH + "/" + cooperativaId + "/cooperados/" + cooperadoId;
        return this.cadastroApiErrorHandler
                .get(path, CooperadoApiResponseDTO.class, "Chamada para buscar cooperado na API de Cadastro")
                .block();
    }

    public CooperativaApiResponseDTO buscarCooperativa(Long cooperativaId) {
        return this.cadastroApiErrorHandler
                .get(
                        COOPERATIVA_PATH + "/" + cooperativaId,
                        CooperativaApiResponseDTO.class,
                        "Chamada para buscar cooperativa na API de Cadastro")
                .block();
    }
}
