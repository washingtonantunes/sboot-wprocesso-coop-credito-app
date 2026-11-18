package br.com.wti.core.auditoria;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionType;

@Getter
@Setter
@AllArgsConstructor
public class AuditoriaResponseDTO {

    private LocalDateTime dataHora;
    private RevisionType tipo;
    private String usuario;
    private Object dados;
}
