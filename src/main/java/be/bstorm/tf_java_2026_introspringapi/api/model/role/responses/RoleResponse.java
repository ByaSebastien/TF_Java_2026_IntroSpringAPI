package be.bstorm.tf_java_2026_introspringapi.api.model.role.responses;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Role;

/**
 * Role sérialisé pour la réponse HTTP.
 * Utilisé dans les DTOs d'utilisateur pour afficher le rôle assigné.
 */
public record RoleResponse(
        Integer id,
        String name
) {

    /**
     * Convertit une entité Role en DTO de réponse.
     * @param role entité Role de la BDD
     * @return RoleResponse
     */
    public static RoleResponse fromRole(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName()
        );
    }
}
