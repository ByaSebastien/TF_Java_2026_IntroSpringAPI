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

@Slf4j
@RestControllerAdvice
public class ExceptionHandlers {

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

    @ExceptionHandler(value = IntroSpringApiException.class)
    public ResponseEntity<?> handleIntroSpringApiException(IntroSpringApiException ex) {

        log.error("IntroSpringApiException: {}", ex.getMessage(), ex);
        log.warn("Attention");
        log.info("Info");

        return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
    }

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

    @ExceptionHandler(
            value = UsernameNotFoundException.class
    )
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {

        log.error("UsernameNotFoundException: {}", ex.getMessage(), ex);

        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(
            value = InvalidDataAccessApiUsageException.class
    )
    public ResponseEntity<?> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {

        log.error("InvalidDataAccessApiUsageException: {}", ex.getMessage(), ex);

        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(
            value = JwtException.class
    )
    public ResponseEntity<?> handleJwtException(JwtException ex) {

        log.error("JwtException: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(
            value = Exception.class
    )
    public ResponseEntity<?> handleException(Exception ex) {

        log.error("Exception: {}", ex.getMessage(), ex);

        return ResponseEntity.internalServerError().build();
    }

}
