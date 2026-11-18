package br.com.wti.creditorural.controller.dto.request;

import br.com.wti.creditorural.domain.enums.TipoDesclassificacaoEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class DesclassificarCreditoRuralRequestDTO {

    @NotNull
    private TipoDesclassificacaoEnum tipoDesclassificacao;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal valorDesclassificado;

    @NotBlank
    @Size(max = 1000)
    private String motivo;
}
