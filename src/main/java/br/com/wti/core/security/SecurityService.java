package br.com.wti.core.security;

import br.com.wti.core.exception.AcessoNegadoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class SecurityService {

    public UsuarioAutenticadoDTO getUsuarioAutenticado() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            return (UsuarioAutenticadoDTO) request.getAttribute("usuarioAutenticado");
        }
        throw AcessoNegadoException.createWith("erro.acesso-negado");
    }
}
