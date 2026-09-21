package be.bstorm.tf_java_2026_introspringapi.api.model.user.requests;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank String password
) {

    public User toUser() {
        return new User(
          username,
          password
        );
    }
}
