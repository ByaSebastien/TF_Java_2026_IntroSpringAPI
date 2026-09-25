package be.bstorm.tf_java_2026_introspringapi.api.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security.
 * Désactive CSRF/CORS pour l'API stateless, accepte toutes les requêtes.
 * La sécurité est basée sur les tokens JWT (filtré par JwtFilter).
 * @PreAuthorize contrôle l'accès par rôle aux endpoints.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Encoder BCrypt pour l'hashage des mots de passe.
     * Force 10 rounds de hachage (16 par défaut, 10 plus rapide).
     * @return PasswordEncoder BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Chaîne de filtres de sécurité pour les requêtes HTTP.
     * - CSRF désactivé (API stateless)
     * - CORS désactivé (à configurer si frontend externe)
     * - Toutes les requêtes autorisées (auth via JWT et @PreAuthorize)
     * @param http configuration HttpSecurity
     * @return SecurityFilterChain configurée
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(r ->
                        r.anyRequest().permitAll()
                );

        return http.build();
    }
}