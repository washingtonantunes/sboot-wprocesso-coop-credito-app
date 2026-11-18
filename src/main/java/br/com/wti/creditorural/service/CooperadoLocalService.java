package br.com.wti.creditorural.service;

import br.com.wti.core.exception.BusinessException;
import br.com.wti.creditorural.domain.entity.Cooperado;
import br.com.wti.creditorural.domain.repository.CooperadoRepository;
import br.com.wti.integracao.cadastro.dto.response.CooperadoApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CooperadoLocalService {

    private final CooperadoRepository cooperadoRepository;

    public Cooperado buscarOuCriar(CooperadoApiResponseDTO resposta, Long cooperativaId) {
        Cooperado cooperado = this.cooperadoRepository
                .findByIdExterno(resposta.getId())
                .orElseGet(() -> {
                    Cooperado novoCooperado = new Cooperado();
                    novoCooperado.setIdExterno(resposta.getId());
                    novoCooperado.setCooperativaId(cooperativaId);
                    return novoCooperado;
                });

        if (!cooperado.getCooperativaId().equals(cooperativaId)) {
            throw BusinessException.createWith("cooperado.nao.encontrado");
        }

        cooperado.setNome(resposta.getNome());
        return this.cooperadoRepository.save(cooperado);
    }
}
