package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Levée lors de l'inscription avec un username déjà utilisé.
 * Retourne 409 CONFLICT au client.
 * Le body contient la liste des champs en conflit.
 */
public class UserAlreadyExistException extends UserException{

    public UserAlreadyExistException() {
        super(HttpStatus.CONFLICT, new HashMap<String,String>(
                Map.of("username", "Username already exists")
        ));
    }

    /**
     * @param body Map des champs en conflit
     */
    public UserAlreadyExistException(Map<String, String> body) {
        super(HttpStatus.CONFLICT, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
