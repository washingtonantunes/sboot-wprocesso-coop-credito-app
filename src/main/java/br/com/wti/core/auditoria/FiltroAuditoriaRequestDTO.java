package br.com.wti.core.auditoria;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class FiltroAuditoriaRequestDTO {

    @NotNull
    private Long id;

    @NotBlank
    private String entidade;

    private LocalDate data;
}
