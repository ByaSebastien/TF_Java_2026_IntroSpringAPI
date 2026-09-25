package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user;

import org.springframework.http.HttpStatus;

/**
 * Levée lors du login avec un password incorrect.
 * Retourne 400 BAD_REQUEST au client.
 * Contexte: authentification, changement de password.
 */
public class UserInvalidPasswordException extends UserException{

    public UserInvalidPasswordException() {
        super(HttpStatus.BAD_REQUEST, "Invalid password");
    }

    /**
     * @param body message d'erreur personnalisé
     */
    public UserInvalidPasswordException(String body) {
        super(HttpStatus.BAD_REQUEST, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
