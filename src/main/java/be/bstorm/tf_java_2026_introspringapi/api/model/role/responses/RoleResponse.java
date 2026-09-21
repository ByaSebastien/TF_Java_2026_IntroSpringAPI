package be.bstorm.tf_java_2026_introspringapi.api.model.role.responses;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Role;

public record RoleResponse(
        Integer id,
        String name
) {

    public static RoleResponse fromRole(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName()
        );
    }
}
