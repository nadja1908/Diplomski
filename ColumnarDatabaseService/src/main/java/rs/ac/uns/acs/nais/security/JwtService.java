package rs.ac.uns.acs.nais.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.config.ColumnarProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final ColumnarProperties properties;

    public JwtService(ColumnarProperties properties) {
        this.properties = properties;
    }

    private SecretKey signingKey() {
        byte[] raw = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = raw[i % Math.max(raw.length, 1)];
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
