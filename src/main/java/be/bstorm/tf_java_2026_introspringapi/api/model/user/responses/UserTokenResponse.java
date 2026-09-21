package be.bstorm.tf_java_2026_introspringapi.api.model.user.responses;

public record UserTokenResponse(
        UserResponse user,
        String token
) {
}
