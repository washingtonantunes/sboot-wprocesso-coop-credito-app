package br.com.wti.creditorural.controller.dto.response;

import br.com.wti.creditorural.domain.enums.StatusOperacaoCreditoRuralEnum;
import br.com.wti.creditorural.domain.enums.TipoDesclassificacaoEnum;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperacaoCreditoRuralResponseDTO {

    @NotNull
    private Long id;

    @NotNull
    private Long cooperativaId;

    @NotNull
    private String cooperativaNome;

    @NotNull
    private Long cooperadoId;

    @NotNull
    private String cooperadoNome;

    @NotNull
    private String produtoCodigo;

    @NotNull
    private BigDecimal valor;

    @NotNull
    private String finalidade;

    @NotNull
    private StatusOperacaoCreditoRuralEnum status;

    private TipoDesclassificacaoEnum tipoDesclassificacao;
    private BigDecimal valorDesclassificado;
    private String motivo;
}
