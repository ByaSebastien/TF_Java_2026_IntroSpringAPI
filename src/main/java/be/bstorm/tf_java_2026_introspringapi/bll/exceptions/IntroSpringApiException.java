package be.bstorm.tf_java_2026_introspringapi.bll.exceptions;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
 * Classe de base abstraite pour les exceptions métier de l'application.
 * Encapsule un HttpStatus et un body pour la réponse HTTP.
 * Permet une gestion centralisée via ExceptionHandler.
 */
@EqualsAndHashCode(callSuper = false) @ToString
public abstract class IntroSpringApiException extends RuntimeException {

    @Getter
    private String section;

    @Getter
    private final HttpStatus status;

    @Getter
    private final Object body;

    /**
     * Constructeur parent.
     * @param status code HTTP de réponse
     * @param body contenu du body pour le client
     * @param section section métier concernée (user, game, etc.)
     */
    public IntroSpringApiException(HttpStatus status, Object body, String section) {
        super();
        this.status = status;
        this.body = body;
        this.section = section;
    }
}
