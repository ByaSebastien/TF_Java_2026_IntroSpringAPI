package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.role;

import org.springframework.http.HttpStatus;

/**
 * Levée quand un rôle demandé n'existe pas en BDD.
 * Retourne 404 NOT_FOUND au client.
 * Contexte: inscription, affectation de rôle, chargement en cache.
 */
public class RoleNotFoundException extends RoleException{

    public RoleNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Role not found");
    }

    /**
     * @param body message d'erreur personnalisé
     */
    public RoleNotFoundException(String body) {
        super(HttpStatus.NOT_FOUND, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
