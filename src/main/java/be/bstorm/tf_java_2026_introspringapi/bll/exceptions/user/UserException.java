package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user;


import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.IntroSpringApiException;
import org.springframework.http.HttpStatus;

/**
 * Classe de base pour les exceptions métier concernant les Users.
 * Scopes les exceptions à la section "user".
 */
public abstract class UserException extends IntroSpringApiException {

    /**
     * @param status code HTTP
     * @param body message/détails pour le client
     */
    public UserException(HttpStatus status, Object body) {
        super(status, body, "user");
    }
}
