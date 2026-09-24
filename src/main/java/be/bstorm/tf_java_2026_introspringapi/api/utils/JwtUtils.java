package be.bstorm.tf_java_2026_introspringapi.api.utils;

import be.bstorm.tf_java_2026_introspringapi.api.model.user.UserContext;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    private final JwtBuilder jwtBuilder;
    private final JwtParser jwtParser;

    public JwtUtils() {
        String jwtSecret = "Yabadabadooooooooooooooooooooooooooooooooooooooooooo";
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        jwtBuilder = Jwts.builder().signWith(secretKey);
        jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    public String generateToken(User user) {

        return jwtBuilder
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole().getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900 * 1000))
                .compact();
    }

    public Claims parseToken(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public Integer getId(String token) {
        return parseToken(token).get("id", Integer.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public UserContext getUser(String token) {
        return new UserContext(
                getId(token),
                getUsername(token),
                getRole(token)
        );
    }

    public boolean validateToken(String token) {
        Claims claims = parseToken(token);

        Date now = new Date();

        return now.after(claims.getIssuedAt()) && now.before(claims.getExpiration());
    }

    public String generateRefreshToken(User user) {
        return jwtBuilder
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole().getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 604800 * 1000))
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token);
    }
}
