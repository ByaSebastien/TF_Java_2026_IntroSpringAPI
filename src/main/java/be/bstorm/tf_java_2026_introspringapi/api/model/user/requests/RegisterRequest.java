package be.bstorm.tf_java_2026_introspringapi.api.model.user.requests;

import be.bstorm.tf_java_2026_introspringapi.api.validators.MinAge;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Données pour l'inscription d'un nouvel utilisateur.
 * Valide le username (longueur), password (pas vide) et age (min 18 ans).
 * Le serveur encode le password et attribue le rôle USER.
 */
public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username max 50 chars")
        String username,
        
        @NotBlank(message = "Password is required")
        String password,
        
        @NotNull(message = "Birthday is required")
        @MinAge(min = 18, message = "Must be at least 18 years old")
        LocalDate birthday
) {

    /**
     * Convertit ce DTO en entité User pour la persistance.
     * Le password ne sera pas encodé ici (traité par le service).
     * @return User sans ID ni rôle (seront attribués par le service)
     */
    public User toUser() {
        return new User(
                username,
                password,
                birthday
        );
    }
}
