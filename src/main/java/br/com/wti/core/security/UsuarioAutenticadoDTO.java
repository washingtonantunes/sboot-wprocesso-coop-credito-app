package br.com.wti.core.security;

import br.com.wti.core.tenant.TipoPerfilEnum;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UsuarioAutenticadoDTO {

    private Long id;
    private String nome;
    private String tipo;

    // Vazia para usuário da Central e obrigatória para usuário de cooperativa.
    private Long cooperativaId;

    private List<String> funcionalidades;
    private Map<String, String> atributos;

    public TipoPerfilEnum getTipoEnum() {
        return tipo != null ? TipoPerfilEnum.valueOf(tipo) : null;
    }

    public boolean temFuncionalidade(String codigo) {
        return funcionalidades != null && funcionalidades.contains(codigo);
    }
}
