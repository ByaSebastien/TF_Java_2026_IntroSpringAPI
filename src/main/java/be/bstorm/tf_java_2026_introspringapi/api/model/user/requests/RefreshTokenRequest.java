package be.bstorm.tf_java_2026_introspringapi.api.model.user.requests;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
