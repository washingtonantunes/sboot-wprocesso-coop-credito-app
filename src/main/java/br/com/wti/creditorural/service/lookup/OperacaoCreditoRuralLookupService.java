package br.com.wti.creditorural.service.lookup;

import br.com.wti.core.exception.EntidadeNaoEncontradaException;
import br.com.wti.core.tenant.EscopoCooperativa;
import br.com.wti.creditorural.domain.entity.OperacaoCreditoRural;
import br.com.wti.creditorural.domain.repository.OperacaoCreditoRuralRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperacaoCreditoRuralLookupService {

    private final OperacaoCreditoRuralRepository operacaoCreditoRuralRepository;

    public OperacaoCreditoRural verificarOperacaoCreditoRural(Long id, EscopoCooperativa escopo) {
        OperacaoCreditoRural operacao = this.operacaoCreditoRuralRepository
                .findById(id)
                .orElseThrow(() -> EntidadeNaoEncontradaException.createWith("operacao.nao.encontrada"));

        if (!escopo.abrange(operacao.getCooperativaId())) {
            throw EntidadeNaoEncontradaException.createWith("operacao.nao.encontrada");
        }

        return operacao;
    }

    public OperacaoCreditoRural verificarOperacaoParaAtualizacao(Long id, EscopoCooperativa escopo) {
        OperacaoCreditoRural operacao = this.operacaoCreditoRuralRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> EntidadeNaoEncontradaException.createWith("operacao.nao.encontrada"));

        if (!escopo.abrange(operacao.getCooperativaId())) {
            throw EntidadeNaoEncontradaException.createWith("operacao.nao.encontrada");
        }

        return operacao;
    }
}
