package be.bstorm.tf_java_2026_introspringapi.dal.repositories;

import be.bstorm.tf_java_2026_introspringapi.dl.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Accès aux données pour les Games.
 * Utilise le pattern Repository: abstrait la persistance et expose des méthodes métier simples.
 * JpaSpecificationExecutor permet les recherches dynamiques complexes via Specifications.
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Integer>, JpaSpecificationExecutor<Game> {
}
