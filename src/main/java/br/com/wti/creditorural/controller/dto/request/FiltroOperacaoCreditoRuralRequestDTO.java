package br.com.wti.creditorural.controller.dto.request;

import br.com.wti.creditorural.domain.enums.StatusOperacaoCreditoRuralEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FiltroOperacaoCreditoRuralRequestDTO {

    private StatusOperacaoCreditoRuralEnum status;
    private String cooperadoNome;
    private String cooperativaNome;
}
