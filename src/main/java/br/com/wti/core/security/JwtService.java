package br.com.wti.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiracao}")
    private Duration expiracao;

    public String gerarToken(
            Long usuarioId,
            String nome,
            String tipo,
            Long cooperativaId,
            List<String> funcionalidades,
            Map<String, String> atributos) {
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("nome", nome)
                .claim("tipo", tipo)
                .claim("cooperativaId", cooperativaId)
                .claim("funcionalidades", funcionalidades)
                .claim("atributos", atributos)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracao.toMillis()))
                .signWith(getChaveAssinatura())
                .compact();
    }

    @SuppressWarnings("unchecked")
    public UsuarioAutenticadoDTO validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getChaveAssinatura())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long id = Long.parseLong(claims.getSubject());
            String nome = claims.get("nome", String.class);
            String tipo = claims.get("tipo", String.class);
            Number cooperativaId = claims.get("cooperativaId", Number.class);
            List<String> funcionalidades = claims.get("funcionalidades", List.class);
            Map<String, String> atributos = claims.get("atributos", Map.class);

            return new UsuarioAutenticadoDTO(
                    id,
                    nome,
                    tipo,
                    cooperativaId != null ? cooperativaId.longValue() : null,
                    funcionalidades,
                    atributos);
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey getChaveAssinatura() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
