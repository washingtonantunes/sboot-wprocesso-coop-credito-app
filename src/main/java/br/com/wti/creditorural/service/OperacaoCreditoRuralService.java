package br.com.wti.creditorural.service;

import br.com.wti.core.exception.AcessoNegadoException;
import br.com.wti.core.exception.BusinessException;
import br.com.wti.core.tenant.EscopoCooperativa;
import br.com.wti.core.tenant.EscopoSpecification;
import br.com.wti.creditorural.controller.dto.request.ContratarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.request.DesclassificarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.request.FiltroOperacaoCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.request.NegarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.response.OperacaoCreditoRuralResponseDTO;
import br.com.wti.creditorural.controller.dto.response.PageResponseDTO;
import br.com.wti.creditorural.domain.entity.Cooperado;
import br.com.wti.creditorural.domain.entity.Cooperativa;
import br.com.wti.creditorural.domain.entity.OperacaoCreditoRural;
import br.com.wti.creditorural.domain.enums.StatusOperacaoCreditoRuralEnum;
import br.com.wti.creditorural.domain.enums.TipoDesclassificacaoEnum;
import br.com.wti.creditorural.domain.repository.OperacaoCreditoRuralRepository;
import br.com.wti.creditorural.domain.specification.OperacaoCreditoRuralSpecification;
import br.com.wti.creditorural.mapper.OperacaoCreditoRuralMapper;
import br.com.wti.creditorural.service.lookup.OperacaoCreditoRuralLookupService;
import br.com.wti.integracao.cadastro.CadastroClient;
import br.com.wti.integracao.cadastro.dto.response.CooperadoApiResponseDTO;
import br.com.wti.integracao.cadastro.dto.response.CooperativaApiResponseDTO;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OperacaoCreditoRuralService {

    private static final String PRODUTO_CODIGO = "CREDITO_RURAL";

    private final OperacaoCreditoRuralRepository operacaoCreditoRuralRepository;
    private final OperacaoCreditoRuralLookupService operacaoCreditoRuralLookupService;
    private final OperacaoCreditoRuralMapper operacaoCreditoRuralMapper;
    private final CadastroClient cadastroClient;
    private final CooperativaLocalService cooperativaLocalService;
    private final CooperadoLocalService cooperadoLocalService;

    @Transactional(readOnly = true)
    public PageResponseDTO<OperacaoCreditoRuralResponseDTO> pesquisarPaginado(
            EscopoCooperativa escopo, FiltroOperacaoCreditoRuralRequestDTO filtro, Pageable pageable) {
        Specification<OperacaoCreditoRural> specification = EscopoSpecification.<OperacaoCreditoRural>noEscopo(escopo)
                .and(OperacaoCreditoRuralSpecification.porFiltro(filtro));
        Page<OperacaoCreditoRuralResponseDTO> page = this.operacaoCreditoRuralRepository
                .findAll(specification, pageable)
                .map(this.operacaoCreditoRuralMapper::toOperacaoCreditoRuralResponseDTO);
        return new PageResponseDTO<>(page);
    }

    @Transactional(readOnly = true)
    public OperacaoCreditoRuralResponseDTO buscar(EscopoCooperativa escopo, Long id) {
        OperacaoCreditoRural operacao =
                this.operacaoCreditoRuralLookupService.verificarOperacaoCreditoRural(id, escopo);
        return this.operacaoCreditoRuralMapper.toOperacaoCreditoRuralResponseDTO(operacao);
    }

    @Transactional
    public OperacaoCreditoRuralResponseDTO contratar(EscopoCooperativa escopo, ContratarCreditoRuralRequestDTO dto) {
        exigirCooperativaUnica(escopo);
        Long cooperativaId = escopo.cooperativaId();

        CooperativaApiResponseDTO cooperativaExterna = this.cadastroClient.buscarCooperativa(cooperativaId);
        CooperadoApiResponseDTO cooperadoExterno =
                this.cadastroClient.buscarCooperado(cooperativaId, dto.getCooperadoId());

        Cooperativa cooperativa = this.cooperativaLocalService.buscarOuCriar(cooperativaExterna);
        Cooperado cooperado = this.cooperadoLocalService.buscarOuCriar(cooperadoExterno, cooperativaId);

        OperacaoCreditoRural operacao = this.operacaoCreditoRuralMapper.toOperacaoCreditoRural(dto);
        operacao.setCooperativaId(cooperativa.getIdExterno());
        operacao.setCooperadoId(cooperado.getIdExterno());
        operacao.setCooperativa(cooperativa);
        operacao.setCooperado(cooperado);
        operacao.setProdutoCodigo(PRODUTO_CODIGO);
        operacao.setStatus(StatusOperacaoCreditoRuralEnum.SOLICITADA);

        OperacaoCreditoRural salva = this.operacaoCreditoRuralRepository.save(operacao);
        return this.operacaoCreditoRuralMapper.toOperacaoCreditoRuralResponseDTO(salva);
    }

    @Transactional
    public void aprovar(EscopoCooperativa escopo, Long id) {
        OperacaoCreditoRural operacao =
                this.operacaoCreditoRuralLookupService.verificarOperacaoParaAtualizacao(id, escopo);
        exigirStatus(operacao, StatusOperacaoCreditoRuralEnum.SOLICITADA);
        operacao.setStatus(StatusOperacaoCreditoRuralEnum.APROVADA);
        this.operacaoCreditoRuralRepository.save(operacao);
    }

    @Transactional
    public void negar(EscopoCooperativa escopo, Long id, NegarCreditoRuralRequestDTO dto) {
        OperacaoCreditoRural operacao =
                this.operacaoCreditoRuralLookupService.verificarOperacaoParaAtualizacao(id, escopo);
        exigirStatus(operacao, StatusOperacaoCreditoRuralEnum.SOLICITADA);
        operacao.setStatus(StatusOperacaoCreditoRuralEnum.NEGADA);
        operacao.setMotivo(dto.getMotivo());
        this.operacaoCreditoRuralRepository.save(operacao);
    }

    @Transactional
    public void desclassificar(EscopoCooperativa escopo, Long id, DesclassificarCreditoRuralRequestDTO dto) {
        OperacaoCreditoRural operacao =
                this.operacaoCreditoRuralLookupService.verificarOperacaoParaAtualizacao(id, escopo);
        exigirStatus(operacao, StatusOperacaoCreditoRuralEnum.APROVADA);
        validarValorDesclassificado(operacao.getValor(), dto.getTipoDesclassificacao(), dto.getValorDesclassificado());
        operacao.setStatus(StatusOperacaoCreditoRuralEnum.DESCLASSIFICADA);
        operacao.setTipoDesclassificacao(dto.getTipoDesclassificacao());
        operacao.setValorDesclassificado(dto.getValorDesclassificado());
        operacao.setMotivo(dto.getMotivo());
        this.operacaoCreditoRuralRepository.save(operacao);
    }

    private void exigirCooperativaUnica(EscopoCooperativa escopo) {
        if (escopo.isTodas()) {
            throw AcessoNegadoException.createWith("erro.acesso-negado");
        }
    }

    private void exigirStatus(OperacaoCreditoRural operacao, StatusOperacaoCreditoRuralEnum statusEsperado) {
        if (operacao.getStatus() != statusEsperado) {
            throw BusinessException.createWith("operacao.status.invalido");
        }
    }

    private void validarValorDesclassificado(
            BigDecimal valorOperacao, TipoDesclassificacaoEnum tipo, BigDecimal valorDesclassificado) {
        boolean valorTotalInvalido =
                tipo == TipoDesclassificacaoEnum.TOTAL && valorDesclassificado.compareTo(valorOperacao) != 0;
        boolean valorParcialInvalido =
                tipo == TipoDesclassificacaoEnum.PARCIAL && valorDesclassificado.compareTo(valorOperacao) >= 0;

        if (valorTotalInvalido || valorParcialInvalido) {
            throw BusinessException.createWith("operacao.valor-desclassificado.invalido");
        }
    }
}
