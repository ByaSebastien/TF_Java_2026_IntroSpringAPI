package be.bstorm.tf_java_2026_introspringapi.api.model.user.responses;

/**
 * Réponse d'authentification retournée après login/register.
 * Contient l'utilisateur, son access token (JWT court terme) et refresh token (long terme).
 * Le client stocke les tokens pour les requêtes suivantes.
 */
public record UserTokenResponse(
        UserResponse user,
        String accessToken,
        String refreshToken
) {
}
