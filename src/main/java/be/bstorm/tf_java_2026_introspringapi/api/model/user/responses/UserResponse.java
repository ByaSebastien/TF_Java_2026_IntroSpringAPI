package be.bstorm.tf_java_2026_introspringapi.api.model.user.responses;

import be.bstorm.tf_java_2026_introspringapi.api.model.role.responses.RoleResponse;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;

/**
 * User sérialisé pour la réponse HTTP.
 * Contient l'ID, username et rôle (mais pas le password!).
 * Utilisé après inscription/login.
 */
public record UserResponse(
        Integer id,
        String username,
        RoleResponse role
) {

    /**
     * Convertit une entité User en DTO de réponse.
     * N'expose pas le password, l'email, etc.
     * @param user entité User de la BDD
     * @return UserResponse sans données sensibles
     */
    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                RoleResponse.fromRole(user.getRole())
        );
    }
}
