package br.com.wti.integracao.cadastro.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CooperativaApiResponseDTO {

    private Long id;
    private String nome;
    private Boolean ativo;
}
