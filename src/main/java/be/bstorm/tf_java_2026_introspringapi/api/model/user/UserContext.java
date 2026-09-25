package be.bstorm.tf_java_2026_introspringapi.api.model.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Contexte utilisateur extrait du JWT après validation.
 * Contient les données minimales de l'utilisateur connecté.
 * Utilisé dans les contrôleurs via @AuthenticationPrincipal.
 */
public record UserContext(
        Integer id,
        String username,
        String role
) {

    /**
     * Retourne les autorités Spring Security de l'utilisateur.
     * @return List avec un seul GrantedAuthority basé sur le rôle
     */
    public List<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }
}
