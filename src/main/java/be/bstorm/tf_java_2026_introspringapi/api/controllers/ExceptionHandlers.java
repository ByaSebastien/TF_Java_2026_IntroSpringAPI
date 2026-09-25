package be.bstorm.tf_java_2026_introspringapi.api.controllers;

import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.IntroSpringApiException;
import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.RateLimitException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sqm.PathElementException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire centralisé des exceptions Spring.
 * Intercepte les exceptions métier et techniques pour retourner des réponses HTTP structurées.
 * Chaque type d'exception reçoit un traitement approprié avec logging.
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlers {

    /**
     * Gère le dépassement de la limite de débit.
     * Retourne 429 avec headers Retry-After pour instruire le client.
     * @param ex exception de rate limit
     * @return 429 TOO_MANY_REQUESTS avec infos de retry
     */
    @ExceptionHandler(value = RateLimitException.class)
    public ResponseEntity<?> handleRateLimitException(RateLimitException ex) {
        log.warn("RateLimitException: {}", ex.getMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Remaining", String.valueOf(ex.getRemainingTokens()));
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(Map.of(
                        "error", "Rate limit exceeded",
                        "message", ex.getBody().toString(),
                        "retryAfter", ex.getRetryAfterSeconds()
                ));
    }

    /**
     * Gère les exceptions métier de l'application.
     * Retourne le HttpStatus et body définis dans l'exception.
     * @param ex exception métier
     * @return réponse HTTP avec statut approprié
     */
    @ExceptionHandler(value = IntroSpringApiException.class)
    public ResponseEntity<?> handleIntroSpringApiException(IntroSpringApiException ex) {

        log.error("IntroSpringApiException: {}", ex.getMessage(), ex);

        return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
    }

    /**
     * Gère les erreurs de validation Jakarta Bean Validation.
     * Retourne 400 avec détail des champs invalides et messages.
     * @param ex exception de validation
     * @return 400 BAD_REQUEST avec Map fieldName -> List[messages]
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        log.warn("MethodArgumentNotValidException: {}", ex.getMessage(), ex);

        Map<String, List<String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())
                ));
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Gère l'absence d'utilisateur en base pour Spring Security.
     * Retourne 404 NOT_FOUND.
     * @param ex exception Spring Security
     * @return 404 NOT_FOUND
     */
    @ExceptionHandler(
            value = UsernameNotFoundException.class
    )
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {

        log.error("UsernameNotFoundException: {}", ex.getMessage(), ex);

        return ResponseEntity.status(404).body(ex.getMessage());
    }

    /**
     * Gère les erreurs d'accès à la BDD (ex: champ introuvable).
     * Retourne 400 BAD_REQUEST.
     * @param ex exception Hibernate/JPA
     * @return 400 BAD_REQUEST
     */
    @ExceptionHandler(
            value = InvalidDataAccessApiUsageException.class
    )
    public ResponseEntity<?> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {

        log.error("InvalidDataAccessApiUsageException: {}", ex.getMessage(), ex);

        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * Gère les erreurs de validation/parsing JWT.
     * Retourne 401 UNAUTHORIZED.
     * @param ex exception JWT
     * @return 401 UNAUTHORIZED
     */
    @ExceptionHandler(
            value = JwtException.class
    )
    public ResponseEntity<?> handleJwtException(JwtException ex) {

        log.error("JwtException: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /**
     * Gère toute autre exception non prévue.
     * Retourne 500 INTERNAL_SERVER_ERROR.
     * @param ex exception générique
     * @return 500 INTERNAL_SERVER_ERROR (sans body détaillé pour sécurité)
     */
    @ExceptionHandler(
            value = Exception.class
    )
    public ResponseEntity<?> handleException(Exception ex) {

        log.error("Exception: {}", ex.getMessage(), ex);

        return ResponseEntity.internalServerError().build();
    }

}
