package be.bstorm.tf_java_2026_introspringapi.dal.repositories;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Accès aux données pour les Users.
 * Utilise le pattern Repository pour isoler la logique de persistance.
 * La requête @Query charge eagerly le rôle pour éviter les N+1 queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Vérifie si un utilisateur existe par son nom unique.
     * @param username nom d'utilisateur
     * @return true si l'utilisateur existe, false sinon
     */
    boolean existsByUsername(String username);

    /**
     * Trouve un utilisateur par son nom avec ses droits (rôle).
     * Utilise FETCH JOIN pour éviter une requête supplémentaire.
     * @param username nom d'utilisateur
     * @return Optional contenant l'utilisateur s'il existe
     */
    @Query("select u from User u join fetch u.role where u.username ilike :username")
    Optional<User> findByUsername(String username);
}
