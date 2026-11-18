package br.com.wti.creditorural.mapper;

import br.com.wti.creditorural.controller.dto.request.ContratarCreditoRuralRequestDTO;
import br.com.wti.creditorural.controller.dto.response.OperacaoCreditoRuralResponseDTO;
import br.com.wti.creditorural.domain.entity.OperacaoCreditoRural;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OperacaoCreditoRuralMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cooperativaId", ignore = true)
    @Mapping(target = "cooperativa", ignore = true)
    @Mapping(target = "cooperadoId", ignore = true)
    @Mapping(target = "cooperado", ignore = true)
    @Mapping(target = "produtoCodigo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "tipoDesclassificacao", ignore = true)
    @Mapping(target = "valorDesclassificado", ignore = true)
    @Mapping(target = "motivo", ignore = true)
    OperacaoCreditoRural toOperacaoCreditoRural(ContratarCreditoRuralRequestDTO dto);

    @Mapping(target = "cooperadoNome", source = "cooperado.nome")
    @Mapping(target = "cooperativaNome", source = "cooperativa.nome")
    OperacaoCreditoRuralResponseDTO toOperacaoCreditoRuralResponseDTO(OperacaoCreditoRural operacao);
}
