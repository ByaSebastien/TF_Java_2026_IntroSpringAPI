package be.bstorm.tf_java_2026_introspringapi.api.controllers;

import be.bstorm.tf_java_2026_introspringapi.api.model.user.requests.LoginRequest;
import be.bstorm.tf_java_2026_introspringapi.api.model.user.requests.RegisterRequest;
import be.bstorm.tf_java_2026_introspringapi.api.model.user.responses.UserResponse;
import be.bstorm.tf_java_2026_introspringapi.api.model.user.responses.UserTokenResponse;
import be.bstorm.tf_java_2026_introspringapi.api.utils.JwtUtils;
import be.bstorm.tf_java_2026_introspringapi.bll.services.AuthService;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    public ResponseEntity<UserTokenResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        User user = authService.register(request.toUser());

        UserTokenResponse response = mapUser(user);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    public ResponseEntity<UserTokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        User user = authService.login(request.username(), request.password());

        UserTokenResponse response = mapUser(user);

        return ResponseEntity.ok(response);
    }

    private UserTokenResponse mapUser(User user) {

        UserResponse userResponse = UserResponse.fromUser(user);

        String token = jwtUtils.generateToken(user);

        return new UserTokenResponse(userResponse, token);
    }
}
