package br.com.wti.core.security;

import br.com.wti.core.auditoria.AuditoriaContextHolder;
import br.com.wti.core.exception.AcessoNegadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Autentica as duas populações que acessam este back-end, separadas por rota:
 *
 * - /api/**  → outros back-ends, token assinado com a chave interna (MICRO_SERVICO)
 * - demais   → usuário logado, token assinado com a chave de usuário
 */
@Component
@RequiredArgsConstructor
public class AutorizacaoInterceptor implements HandlerInterceptor {

    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final JwtServicoInternoService jwtServicoInternoService;
    private final AntPathMatcher antPathMatcher;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        String path = request.getServletPath();

        if (isPathExcluida(path)) {
            return true;
        }

        String token = extrairToken(request);

        if (isPathServicoInterno(path)) {
            return autenticarServicoInterno(token);
        }

        return autenticarUsuario(request, handler, token);
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuditoriaContextHolder.clear();
    }

    private String extrairToken(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");

        if (cabecalho == null || !cabecalho.startsWith(PREFIXO_BEARER)) {
            throw AcessoNegadoException.createWith("erro.token-ausente");
        }

        return cabecalho.substring(PREFIXO_BEARER.length());
    }

    private boolean autenticarServicoInterno(String token) {
        if (!jwtServicoInternoService.isTokenMicroServicoValido(token)) {
            throw AcessoNegadoException.createWith("erro.token-invalido");
        }

        AuditoriaContextHolder.setUsuario("Sistema");
        return true;
    }

    private boolean autenticarUsuario(HttpServletRequest request, Object handler, String token) {
        UsuarioAutenticadoDTO usuarioAutenticado = jwtService.validarToken(token);

        if (usuarioAutenticado == null) {
            throw AcessoNegadoException.createWith("erro.token-invalido");
        }

        request.setAttribute("usuarioAutenticado", usuarioAutenticado);
        AuditoriaContextHolder.setUsuario(usuarioAutenticado.getNome());

        if (handler instanceof HandlerMethod handlerMethod) {
            FuncionalidadeAutorizada annotation = handlerMethod.getMethodAnnotation(FuncionalidadeAutorizada.class);
            if (annotation == null) {
                annotation = handlerMethod.getBeanType().getAnnotation(FuncionalidadeAutorizada.class);
            }

            if (annotation != null) {
                String[] codigosNecessarios = annotation.value();
                boolean autorizado = Arrays.stream(codigosNecessarios).anyMatch(usuarioAutenticado::temFuncionalidade);
                if (!autorizado) {
                    throw new AcessoNegadoException(
                            "Acesso negado para a funcionalidade: " + String.join(", ", codigosNecessarios));
                }
            }
        }

        return true;
    }

    private boolean isPathServicoInterno(String path) {
        return antPathMatcher.match("/api/**", path);
    }

    private boolean isPathExcluida(String path) {
        return antPathMatcher.match("/auth/login", path)
                || antPathMatcher.match("/swagger-ui/**", path)
                || antPathMatcher.match("/v3/api-docs/**", path)
                || antPathMatcher.match("/swagger-resources/**", path)
                || antPathMatcher.match("/webjars/**", path)
                || antPathMatcher.match("/h2-console/**", path);
    }
}
