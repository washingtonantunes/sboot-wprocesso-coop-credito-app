package br.com.wti.creditorural.service;

import br.com.wti.creditorural.domain.entity.Cooperativa;
import br.com.wti.creditorural.domain.repository.CooperativaRepository;
import br.com.wti.integracao.cadastro.dto.response.CooperativaApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CooperativaLocalService {

    private final CooperativaRepository cooperativaRepository;

    public Cooperativa buscarOuCriar(CooperativaApiResponseDTO resposta) {
        Cooperativa cooperativa = this.cooperativaRepository
                .findByIdExterno(resposta.getId())
                .orElseGet(() -> {
                    Cooperativa novaCooperativa = new Cooperativa();
                    novaCooperativa.setIdExterno(resposta.getId());
                    return novaCooperativa;
                });
        cooperativa.setNome(resposta.getNome());
        return this.cooperativaRepository.save(cooperativa);
    }
}
