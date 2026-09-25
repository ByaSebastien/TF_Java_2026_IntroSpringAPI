package be.bstorm.tf_java_2026_introspringapi.dal.repositories;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Accès aux données pour les Roles.
 * Utilise le pattern Repository pour isoler la logique de persistance.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Trouve un rôle par son nom.
     * @param name nom unique du rôle (ex: USER, ADMIN)
     * @return Optional contenant le rôle s'il existe
     */
    Optional<Role> findByName(String name);
}
