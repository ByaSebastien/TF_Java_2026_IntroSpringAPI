package be.bstorm.tf_java_2026_introspringapi.api.model.user.requests;

import be.bstorm.tf_java_2026_introspringapi.api.validators.MinAge;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank String password,
        @NotNull @MinAge(min = 18) LocalDate birthday
) {

    public User toUser() {
        return new User(
                username,
                password,
                birthday
        );
    }
}
