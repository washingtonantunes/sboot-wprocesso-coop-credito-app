package br.com.wti.core.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServiceTokenProvider {

    @Value("${jwt.interno.secret}")
    private String secret;

    // TTL curto; re-emite quando perto de expirar
    private static final long TTL_MS = 300_000; // 5 min
    private String tokenCache;
    private long expiraEm;

    public synchronized String getToken() {
        long agora = System.currentTimeMillis();
        if (tokenCache == null || agora > expiraEm - 30_000) {
            tokenCache = gerar(agora);
            expiraEm = agora + TTL_MS;
        }
        return tokenCache;
    }

    private String gerar(long agora) {
        return Jwts.builder()
                .subject("0") // id "de serviço"
                .claim("nome", "Sistema")
                .claim("tipo", "INTERNO") // valor seguro p/ o enum, se algum fluxo fizer valueOf
                .claim("funcionalidades", List.of("MICRO_SERVICO"))
                .claim("atributos", Map.of())
                .issuedAt(new Date(agora))
                .expiration(new Date(agora + TTL_MS))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
