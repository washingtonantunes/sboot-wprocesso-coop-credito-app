package br.com.wti.core.tenant;

import br.com.wti.core.exception.AcessoNegadoException;
import br.com.wti.core.security.SecurityService;
import br.com.wti.core.security.UsuarioAutenticadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantContext {

    private final SecurityService securityService;

    /**
     * Devolve o escopo de dados da requisição. Este é o único ponto do back em que o
     * tipo do usuário decide o recorte por cooperativa.
     *
     * <ul>
     *   <li>Usuário de cooperativa: sempre a cooperativa do token; a cooperativa
     *       informada na requisição é desconsiderada.
     *   <li>Usuário da Central: todas as cooperativas ou, quando informa uma, apenas ela.
     * </ul>
     *
     * @param cooperativaInformada cooperativa recebida na requisição, ou nulo quando o
     *     endpoint não recebe nenhuma
     */
    public EscopoCooperativa escopo(Long cooperativaInformada) {
        UsuarioAutenticadoDTO usuario = securityService.getUsuarioAutenticado();
        TipoPerfilEnum tipo = usuario != null ? usuario.getTipoEnum() : null;

        if (tipo == TipoPerfilEnum.COOPERATIVA && usuario.getCooperativaId() != null) {
            return EscopoCooperativa.daCooperativa(usuario.getCooperativaId());
        }
        if (tipo == TipoPerfilEnum.CENTRAL) {
            return cooperativaInformada != null
                    ? EscopoCooperativa.daCooperativa(cooperativaInformada)
                    : EscopoCooperativa.todas();
        }
        throw AcessoNegadoException.createWith("erro.acesso-negado");
    }

    /**
     * Devolve o tipo do usuário da requisição. É o ponto único em que o tipo é lido
     * para recortar dados globais que existem por tipo (como as funcionalidades, que
     * não pertencem a uma cooperativa). Controllers e services recebem o tipo pronto e
     * o repassam às consultas, sem decidir por ele.
     */
    public TipoPerfilEnum tipoUsuario() {
        UsuarioAutenticadoDTO usuario = securityService.getUsuarioAutenticado();
        TipoPerfilEnum tipo = usuario != null ? usuario.getTipoEnum() : null;
        if (tipo == null) {
            throw AcessoNegadoException.createWith("erro.acesso-negado");
        }
        return tipo;
    }
}
