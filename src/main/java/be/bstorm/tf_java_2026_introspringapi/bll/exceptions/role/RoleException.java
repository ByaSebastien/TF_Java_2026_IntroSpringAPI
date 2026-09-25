package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.role;

import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.IntroSpringApiException;
import org.springframework.http.HttpStatus;

/**
 * Classe de base pour les exceptions métier concernant les Roles.
 * Scopes les exceptions à la section "role".
 */
public abstract class RoleException extends IntroSpringApiException {

    /**
     * @param status code HTTP
     * @param body message/détails pour le client
     */
    public RoleException(HttpStatus status, Object body) {
        super(status, body, "role");
    }
}
