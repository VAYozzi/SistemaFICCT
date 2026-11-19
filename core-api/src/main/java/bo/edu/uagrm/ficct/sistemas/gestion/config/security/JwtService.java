package bo.edu.uagrm.ficct.sistemas.gestion.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    @Value("${jwt.secret:9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b}")
    private String jwtSecret;

    @Value("${jwt.expiration-hours:8}")
    private long jwtExpirationHours;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generarToken(Long usuarioId, String correo, String rolCodigo) {
        long now = System.currentTimeMillis();
        long expiration = now + (jwtExpirationHours * 3600 * 1000);

        return Jwts.builder()
            .subject(correo)
            .claim("usuarioId", usuarioId)
            .claim("rol", rolCodigo)
            .issuedAt(new Date(now))
            .expiration(new Date(expiration))
            .signWith(getSigningKey())
            .compact();
    }

    public String extraerCorreo(String token) {
        return obtenerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return obtenerClaims(token).get("rol", String.class);
    }

    public Long extraerUsuarioId(String token) {
        return obtenerClaims(token).get("usuarioId", Long.class);
    }

    public boolean esTokenValido(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
