package be.bstorm.tf_java_2026_introspringapi.api.model.user.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête pour renouveler un access token expiré.
 * Utilise le refresh token stocké côté client.
 * Évite à l'utilisateur de re-saisir ses identifiants.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
