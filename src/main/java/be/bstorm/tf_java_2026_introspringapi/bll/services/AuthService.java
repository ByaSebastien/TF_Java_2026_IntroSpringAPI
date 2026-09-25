package be.bstorm.tf_java_2026_introspringapi.bll.services;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Gère l'authentification et l'inscription des utilisateurs.
 * Valide les credentials et coordonne la persistance des données utilisateur.
 * Intégrée à Spring Security pour les vérifications d'accès.
 */
public interface AuthService extends UserDetailsService {

    /**
     * Enregistre un nouvel utilisateur.
     * Valide que le username est unique et encode le password.
     * @param user objet User avec username, password et birthday
     * @return l'utilisateur créé avec ID généré et rôle attribué
     * @throws UserAlreadyExistException si le username existe déjà
     */
    User register(User user);

    /**
     * Authentifie un utilisateur.
     * Valide les credentials username/password.
     * @param username nom d'utilisateur
     * @param password mot de passe en clair
     * @return l'utilisateur trouvé
     * @throws UserNotFoundException si username n'existe pas
     * @throws UserInvalidPasswordException si password est incorrect
     */
    User login(String username, String password);
}
