package be.bstorm.tf_java_2026_introspringapi.api.controllers;

import be.bstorm.tf_java_2026_introspringapi.api.model.user.UserContext;
import be.bstorm.tf_java_2026_introspringapi.api.model.user.requests.LoginRequest;
import be.bstorm.tf_java_2026_introspringapi.api.model.user.requests.RegisterRequest;
import be.bstorm.tf_java_2026_introspringapi.api.model.user.requests.RefreshTokenRequest;
import be.bstorm.tf_java_2026_introspringapi.api.model.user.responses.UserResponse;
import be.bstorm.tf_java_2026_introspringapi.api.model.user.responses.UserTokenResponse;
import be.bstorm.tf_java_2026_introspringapi.api.utils.JwtUtils;
import be.bstorm.tf_java_2026_introspringapi.api.utils.RateLimit;
import be.bstorm.tf_java_2026_introspringapi.bll.services.AuthService;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @RateLimit(maxRequests = 3, windowSeconds = 60)
    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    public ResponseEntity<UserTokenResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        User user = authService.register(request.toUser());

        UserTokenResponse response = mapUser(user);

        return ResponseEntity.ok(response);
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    public ResponseEntity<UserTokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        User user = authService.login(request.username(), request.password());

        UserTokenResponse response = mapUser(user);

        log.info("User {} logged in successfully", user.getUsername());

        return ResponseEntity.ok(response);
    }

    @RateLimit(maxRequests = 10, windowSeconds = 60)
    @PreAuthorize("isAnonymous()")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        if (!jwtUtils.validateRefreshToken(request.refreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        UserContext userContext = jwtUtils.getUser(request.refreshToken());

        User user = (User) authService.loadUserByUsername(userContext.username());

        String newAccessToken = jwtUtils.generateToken(user);

        return ResponseEntity.ok(new UserTokenResponse(
                UserResponse.fromUser(user),
                newAccessToken,
                request.refreshToken()
        ));
    }

    private UserTokenResponse mapUser(User user) {

        UserResponse userResponse = UserResponse.fromUser(user);

        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        return new UserTokenResponse(userResponse, accessToken, refreshToken);
    }
}
