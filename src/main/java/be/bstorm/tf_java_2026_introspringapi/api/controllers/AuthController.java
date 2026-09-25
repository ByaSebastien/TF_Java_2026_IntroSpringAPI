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

/**
 * Endpoints REST pour l'authentification des utilisateurs.
 * Gère l'inscription, login et renouvellement de tokens JWT.
 * Les endpoints sont protégés par rate limiting pour éviter les abus.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    /**
     * Inscrit un nouvel utilisateur.
     * Valide le username unique, password et age minimum (18 ans).
     * Retourne les tokens JWT immédiatement après succès.
     * @param request données d'inscription (username, password, birthday)
     * @return 200 OK avec UserTokenResponse
     * @throws UserAlreadyExistException si username existe déjà
     */
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

    /**
     * Authentifie un utilisateur (login).
     * Valide le username et password, génère les tokens JWT.
     * @param request identifiants (username, password)
     * @return 200 OK avec UserTokenResponse
     * @throws UserNotFoundException si username n'existe pas
     * @throws UserInvalidPasswordException si password est incorrect
     */
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

    /**
     * Renouvelle l'access token avec un refresh token valide.
     * Évite au client de re-saisir ses identifiants.
     * @param request refresh token stocké côté client
     * @return 200 OK avec nouvel access token, ou 401 si refresh token expiré
     */
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

    /**
     * Helper: convertit un User en UserTokenResponse avec tokens JWT.
     * @param user entité User
     * @return UserTokenResponse avec access + refresh tokens
     */
    private UserTokenResponse mapUser(User user) {

        UserResponse userResponse = UserResponse.fromUser(user);

        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        return new UserTokenResponse(userResponse, accessToken, refreshToken);
    }
}
