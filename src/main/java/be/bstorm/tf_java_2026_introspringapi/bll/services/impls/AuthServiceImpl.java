package be.bstorm.tf_java_2026_introspringapi.bll.services.impls;

import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.role.RoleNotFoundException;
import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user.UserAlreadyExistException;
import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user.UserInvalidPasswordException;
import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user.UserNotFoundException;
import be.bstorm.tf_java_2026_introspringapi.bll.services.AuthService;
import be.bstorm.tf_java_2026_introspringapi.dal.repositories.RoleRepository;
import be.bstorm.tf_java_2026_introspringapi.dal.repositories.UserRepository;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.Role;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implémentation de la logique d'authentification et de gestion de sécurité utilisateur.
 * 
 * Responsabilités principales:
 * - Enregistrement: validation de l'unicité du username + hashage du password avec BCrypt
 * - Connexion: vérification des identifiants + retour des données utilisateur
 * - UserDetailsService: intégration avec Spring Security pour charger les utilisateurs
 * 
 * Le password n'est JAMAIS stocké en clair en base de données. Il est toujours hashé
 * avant la sauvegarde. La comparaison utilise passwordEncoder.matches() (ne pas faire ==).
 * 
 * Pattern: Service implémente AuthService (interface métier) + UserDetailsService (interface Spring).
 * Cela permet à Spring Security de charger les utilisateurs sans dépendre directement du service.
 * 
 * @see AuthService interface métier
 * @see UserDetailsService interface Spring Security
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(User user) {
        // Vérification 1: l'username existe-t-il déjà? 
        // Chaque user DOIT avoir un username unique (contrainte métier)
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException();
        }

        // Vérification 2: hasher le password avec BCrypt
        // BCrypt ajoute automatiquement un "salt" aléatoire, rendant chaque hash unique
        // même si le password est le même (sécurité renforcée)
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Vérification 3: récupérer le rôle par défaut "user"
        // Chaque utilisateur DOIT avoir un rôle (jamais null)
        // Si le rôle n'existe pas, lancer une exception (erreur de configuration)
        Role role = roleRepository.findByName("user")
                .orElseThrow(() -> new RoleNotFoundException("Role 'user' not found"));

        user.setRole(role);

        // Sauvegarder l'utilisateur en base de données avec password hashé + rôle assigné
        return userRepository.save(user);
    }

    @Override
    public User login(String username, String password) {
        // Vérification 1: l'utilisateur existe-t-il?
        // findByUsername retourne un Optional (peut être vide ou contenir un User)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        // Vérification 2: le password est-il correct?
        // IMPORTANT: utiliser passwordEncoder.matches() et NON ==
        // matches() utilise BCrypt pour comparer le plain-text reçu avec le hash en BDD
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new UserInvalidPasswordException("Invalid password for user " + username);
        }

        // Retourner l'utilisateur trouvé et validé
        // Ce User sera ensuite utilisé pour générer un JWT dans le contrôleur
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Interface Spring Security: chargement automatique de l'utilisateur
        // Spring appelle cette méthode lors de l'authentification
        // UserDetails = interface Spring qui encapsule User + les GrantedAuthority (rôles)
        // Retourne le User (qui implémente UserDetails grâce aux annotations)
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
    }
}
