package br.com.wti.creditorural.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class NegarCreditoRuralRequestDTO {

    @NotBlank
    @Size(max = 1000)
    private String motivo;
}
