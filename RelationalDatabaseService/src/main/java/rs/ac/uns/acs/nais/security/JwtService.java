package rs.ac.uns.acs.nais.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.config.NaisProperties;
import rs.ac.uns.acs.nais.domain.Korisnik;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final NaisProperties naisProperties;

    public JwtService(NaisProperties naisProperties) {
        this.naisProperties = naisProperties;
    }

    private SecretKey signingKey() {
        byte[] raw = naisProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = raw[i % Math.max(raw.length, 1)];
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Korisnik korisnik) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(korisnik.getId()))
                .claim("role", korisnik.getUloga().name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + naisProperties.getJwt().getExpirationMs()))
                .signWith(signingKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
