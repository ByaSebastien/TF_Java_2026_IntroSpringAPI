package be.bstorm.tf_java_2026_introspringapi.api.model.user.responses;

import be.bstorm.tf_java_2026_introspringapi.api.model.role.responses.RoleResponse;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;

public record UserResponse(
        Integer id,
        String username,
        RoleResponse role
) {

    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                RoleResponse.fromRole(user.getRole())
        );
    }
}
