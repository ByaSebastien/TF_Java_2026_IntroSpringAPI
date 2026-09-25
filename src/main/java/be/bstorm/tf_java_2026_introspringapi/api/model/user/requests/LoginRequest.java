package be.bstorm.tf_java_2026_introspringapi.api.model.user.requests;

/**
 * Identifiants de connexion envoyés par le client.
 * Validés contre la BDD pour l'authentification.
 */
public record LoginRequest(
        String username,
        String password
) {
}
