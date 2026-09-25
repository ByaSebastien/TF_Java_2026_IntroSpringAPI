package be.bstorm.tf_java_2026_introspringapi.api.utils;

import be.bstorm.tf_java_2026_introspringapi.api.model.user.UserContext;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Utilitaire pour la génération et validation de tokens JWT.
 * Encode les credentials utilisateur dans le token.
 * Access tokens: 15 minutes, Refresh tokens: 7 jours.
 */
@Component
public class JwtUtils {

    private final JwtBuilder jwtBuilder;
    private final JwtParser jwtParser;

    private final long accessTokenValidity; // 15 minutes
    private final long refreshTokenValidity; // 7 days

    public JwtUtils(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.accessTokenValidity}") long accessTokenValidity,
            @Value("${jwt.refreshTokenValidity}") long refreshTokenValidity
    ) {
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;

        SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        jwtBuilder = Jwts.builder().signWith(secretKey);
        jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    /**
     * Génère un access token JWT court terme.
     * @param user entité User avec ses credentials
     * @return token JWT signé
     */
    public String generateToken(User user) {

        return jwtBuilder
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole().getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenValidity * 1000))
                .compact();
    }

    /**
     * Parse les claims d'un token JWT.
     * @param token JWT signé
     * @return Claims extraites
     * @throws JwtException si token invalide/expiré
     */
    public Claims parseToken(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    /**
     * Extrait le username du token.
     * @param token JWT
     * @return username (subject)
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extrait l'ID utilisateur du token.
     * @param token JWT
     * @return ID utilisateur
     */
    public Integer getId(String token) {
        return parseToken(token).get("id", Integer.class);
    }

    /**
     * Extrait le rôle du token.
     * @param token JWT
     * @return nom du rôle (USER, ADMIN, etc)
     */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * Extrait toutes les infos utilisateur du token.
     * @param token JWT
     * @return UserContext avec id, username, role
     */
    public UserContext getUser(String token) {
        return new UserContext(
                getId(token),
                getUsername(token),
                getRole(token)
        );
    }

    /**
     * Valide qu'un token est signé correctement et pas expiré.
     * @param token JWT
     * @return true si valide
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);

        Date now = new Date();

        return now.after(claims.getIssuedAt()) && now.before(claims.getExpiration());
    }

    /**
     * Génère un refresh token JWT long terme.
     * Utilisé pour renouveler l'access token sans credentials.
     * @param user entité User
     * @return token JWT signé (validité 7 jours)
     */
    public String generateRefreshToken(User user) {
        return jwtBuilder
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole().getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenValidity * 1000))
                .compact();
    }

    /**
     * Valide un refresh token.
     * @param token JWT
     * @return true si valide
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token);
    }
}
