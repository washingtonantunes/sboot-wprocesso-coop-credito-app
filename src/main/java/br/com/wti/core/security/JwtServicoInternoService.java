package br.com.wti.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Valida o token dos demais back-ends do sistema (chave do canal interno),
 * usado nos endpoints da camada api/.
 */
@Service
public class JwtServicoInternoService {

    private static final String FUNCIONALIDADE_MICRO_SERVICO = "MICRO_SERVICO";

    @Value("${jwt.interno.secret}")
    private String secret;

    @SuppressWarnings("unchecked")
    public boolean isTokenMicroServicoValido(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getChaveAssinatura())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            List<String> funcionalidades = claims.get("funcionalidades", List.class);

            return funcionalidades != null && funcionalidades.contains(FUNCIONALIDADE_MICRO_SERVICO);
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getChaveAssinatura() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
