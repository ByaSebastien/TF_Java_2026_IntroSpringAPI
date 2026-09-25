//package be.bstorm.tf_java_2026_introspringapi.dal.initializers;
//
//import be.bstorm.tf_java_2026_introspringapi.dal.repositories.GameRepository;
//import be.bstorm.tf_java_2026_introspringapi.dal.repositories.RoleRepository;
//import be.bstorm.tf_java_2026_introspringapi.dal.repositories.UserRepository;
//import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
//import be.bstorm.tf_java_2026_introspringapi.dl.entities.Role;
//import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * Initialise les données de la base de données au démarrage de l'application.
// * Crée des rôles (USER, ADMIN) et des utilisateurs de test si la BDD est vide.
// * Utile pour le développement et les tests automatisés.
// */
//@Component
//@RequiredArgsConstructor
//public class Seed implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final RoleRepository roleRepository;
//    private final GameRepository gameRepository;
//
//    /**
//     * Exécuté au démarrage de Spring.
//     * Insère les données initiales si la BDD est vide.
//     * @param args arguments de ligne de commande
//     */
//    @Override
//    public void run(String... args) throws Exception {
//
//        if(userRepository.count() == 0) {
//            Role userRole = new Role("user");
//            Role adminRole = new Role("admin");
//
//            roleRepository.save(userRole);
//            roleRepository.save(adminRole);
//
//            String password = passwordEncoder.encode("Test1234=");
//
//            List<User> users = List.of(
//                    new User(
//                            "user",
//                            password,
//                            userRole
//                    ),
//                    new User(
//                            "admin",
//                            password,
//                            adminRole
//                    )
//            );
//
//            userRepository.saveAll(users);
//        }
//
//        if(gameRepository.count() == 0) {
//
//            List<Game> games = List.of(
//                    new Game("Devil may cry",2001, 5000),
//                    new Game("Onimusha",2001, 3000),
//                    new Game("Resident evil",1996, 10000),
//                    new Game("League of legend",2009, 1000000)
//            );
//
//            gameRepository.saveAll(games);
//        }
//    }
//}
