package File.Node.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;  // injected from application.properties

    private final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 1 day

    // =============================
    // Generate JWT token (email only)
    // =============================
    public String generateToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // =============================
    // Generate JWT token with email + username claim
    // =============================
    public String generateToken(String email, String username) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

        return Jwts.builder()
                .setSubject(email)
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // =============================
    // Extract email from token
    // =============================
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // =============================
    // Validate token
    // =============================
    public boolean validateToken(String token) {
        try {
            return getClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // =============================
    // Get claims from token
    // =============================
    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
