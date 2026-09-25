package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user;

import org.springframework.http.HttpStatus;

/**
 * Levée quand un utilisateur n'existe pas en BDD.
 * Retourne 404 NOT_FOUND au client.
 * Contexte: login, affichage profil, opérations sécurisées.
 */
public class UserNotFoundException extends UserException{

    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }

    /**
     * @param body message d'erreur personnalisé
     */
    public UserNotFoundException(String body) {
        super(HttpStatus.NOT_FOUND, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
